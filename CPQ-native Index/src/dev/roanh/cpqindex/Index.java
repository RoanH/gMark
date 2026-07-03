/*
 * CPQ-native Index: A graph database index with native support for CPQs.
 * Copyright (C) 2023  Roan Hofland (roan@roanh.dev).  All rights reserved.
 * GitHub Repository: https://github.com/RoanH/CPQ-native-index
 *
 * CPQ-native Index is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CPQ-native Index is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package dev.roanh.cpqindex;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import dev.roanh.cpqindex.CanonForm.CoreHash;
import dev.roanh.gmark.lang.cpq.CPQ;
import dev.roanh.gmark.lang.cpq.QueryGraphCPQ;
import dev.roanh.gmark.type.schema.Predicate;
import dev.roanh.gmark.util.RangeList;
import dev.roanh.gmark.util.graph.generic.UniqueGraph;
import dev.roanh.gmark.util.graph.generic.UniqueGraph.GraphEdge;

/**
 * Implementation of a graph database index based on k-path-bisimulation
 * and with support for indexing by CPQ cores. This index is inspired by the
 * path index proposed by Yuya Sasaki, George Fletcher and Makoto Onizuka.
 * @author Roan
 * @see <a href="https://doi.org/10.1109/ICDE53745.2022.00054">Yuya Sasaki, George Fletcher and Makoto Onizuka,
 *      "Language-aware indexing for conjunctive path queries", in IEEE 38th ICDE, 2022</a>
 * @see <a href="https://github.com/yuya-s/CPQ-aware-index">yuya-s/CPQ-aware-index</a>
 */
public class Index{
	/**
	 * Boolean indicating whether explicit representations of cores
	 * and label sequences should be saved for the computed blocks.
	 * Saving these will take significantly more memory and is only
	 * really relevant when printing the index with {@link #print()},
	 * calling {@link Block#getCores()} or calling {@link Block#getLabels()}.
	 */
	private final boolean computeLabels;
	/**
	 * The value of k (the CPQ diameter) this index was computed for.
	 */
	private final int k;
	/**
	 * The maximum number of same layer CPQs allowed in a single intersection.
	 */
	private int maxIntersections;
	/**
	 * Whether CPQ cores should be or have been computed for this index.
	 */
	private boolean computeCores;
	/**
	 * List of predicates (labels) that appear in this index by ID.
	 */
	private RangeList<Predicate> predicates;
	/**
	 * List of blocks in this index by layer (index 0 is k = 1, etc).
	 */
	private RangeList<List<Block>> layers;
	/**
	 * List of all blocks in the final layer of this index.
	 * This is the layer for k equal to {@link #k}.
	 */
	private List<Block> blocks;
	/**
	 * Map from CPQ core hash to the blocks this CPQ is present in.
	 */
	private Map<CoreHash, List<Block>> coreToBlock = new HashMap<CoreHash, List<Block>>();
	/**
	 * Progress listener to inform of any computation updates.
	 */
	private ProgressListener progress;
	
	/**
	 * Constructs a new CPQ-native index for the given graph and diameter.
	 * @param g The graph to compute and index for.
	 * @param k The CPQ diameter k to compute the index for.
	 * @param threads The number of CPU threads to use for computing cores.
	 * @throws IllegalArgumentException When k is less than 1.
	 * @throws InterruptedException When the current thread is interrupted during core computation.
	 */
	public Index(UniqueGraph<Integer, Predicate> g, int k, int threads) throws IllegalArgumentException, InterruptedException{
		this(g, k, threads, Integer.MAX_VALUE);
	}
	
	/**
	 * Constructs a new CPQ-native index for the given graph, diameter and diameter limit.
	 * @param g The graph to compute and index for.
	 * @param k The CPQ diameter k to compute the index for.
	 * @param threads The number of CPU threads to use for computing cores.
	 * @param maxIntersections The maximum number of same level CPQs allowed in intersections.
	 *        Limiting intersection CPQs greatly decreases the number of cores that need to be computed.
	 * @throws IllegalArgumentException When k is less than 1.
	 * @throws InterruptedException When the current thread is interrupted during core computation.
	 */
	public Index(UniqueGraph<Integer, Predicate> g, int k, int threads, int maxIntersections) throws IllegalArgumentException, InterruptedException{
		this(g, k, true, false, threads, maxIntersections, ProgressListener.NONE);
	}
	
	/**
	 * Constructs a new CPQ-native index for the given graph, diameter.
	 * @param g The graph to compute and index for.
	 * @param k The CPQ diameter k to compute the index for.
	 * @param computeCores True to compute cores, if false cores are not computed
	 *        and can instead later be computed using {@link #computeCores(int)} if desired.
	 * @param computeLabels True to compute core and label sequence labels for each index block.
	 * @param threads The number of CPU threads to use for computing cores.
	 * @throws IllegalArgumentException When k is less than 1.
	 * @throws InterruptedException When the current thread is interrupted during core computation.
	 * @see #computeCores(int)
	 */
	public Index(UniqueGraph<Integer, Predicate> g, int k, boolean computeCores, boolean computeLabels, int threads) throws IllegalArgumentException, InterruptedException{
		this(g, k, computeCores, computeLabels, threads, Integer.MAX_VALUE, ProgressListener.NONE);
	}
	
	/**
	 * Constructs a new CPQ-native index for the given graph, diameter.
	 * @param g The graph to compute and index for.
	 * @param k The CPQ diameter k to compute the index for.
	 * @param computeCores True to compute cores, if false cores are not computed
	 *        and can instead later be computed using {@link #computeCores(int)} if desired.
	 * @param computeLabels True to compute core and label sequence labels for each index block.
	 * @param threads The number of CPU threads to use for computing cores.
	 * @param maxIntersections The maximum number of same level CPQs allowed in intersections.
	 *        Limiting intersection CPQs greatly decreases the number of cores that need to be computed.
	 * @param listener The progress listener to send computation progress updates to.
	 * @throws IllegalArgumentException When k is less than 1.
	 * @throws InterruptedException When the current thread is interrupted during core computation.
	 * @see #computeCores(int)
	 * @see ProgressListener
	 */
	public Index(UniqueGraph<Integer, Predicate> g, int k, boolean computeCores, boolean computeLabels, int threads, int maxIntersections, ProgressListener listener) throws IllegalArgumentException, InterruptedException{
		this.computeLabels = computeLabels;
		this.maxIntersections = maxIntersections;
		this.k = k;
		layers = new RangeList<List<Block>>(k, ArrayList::new);
		blocks = layers.get(k - 1);
		setProgressListener(listener == null ? ProgressListener.NONE : listener);
		
		computeBlocks(partition(g));
		if(computeCores){
			computeCores(threads);
			this.computeCores = true;
		}else{
			this.computeCores = false;
		}
	}
	
	/**
	 * Reads a previously saved index. Any previously
	 * attached progress listeners will be detached.
	 * @param source The input stream to read from.
	 * @throws IOException When an IOException occurs.
	 * @see #setProgressListener(ProgressListener)
	 */
	public Index(InputStream source) throws IOException{
		DataInputStream in = new DataInputStream(source);
		boolean full = in.readBoolean();
		computeCores = in.readBoolean();
		computeLabels = in.readBoolean();
		maxIntersections = in.readInt();
		k = in.readInt();
		progress = ProgressListener.NONE;
		
		if(full){
			predicates = new RangeList<Predicate>(in.readInt());
			for(int i = 0; i < predicates.size(); i++){
				byte[] data = new byte[in.readInt()];
				in.readFully(data);
				predicates.set(i, new Predicate(i, new String(data, StandardCharsets.UTF_8)));
			}
		}
		
		RangeList<Block> blockMap = new RangeList<Block>(in.readInt());
		layers = new RangeList<List<Block>>(k, ArrayList::new);
		blocks = layers.get(k - 1);
		for(int i = full ? 0 : (k - 1); i < k; i++){
			List<Block> layer = layers.get(i);
			int len = in.readInt();
			for(int j = 0; j < len; j++){
				Block block = new Block(in, full, blockMap);
				layer.add(block);
				blockMap.set(block.getId(), block);
			}
		}

		int len = in.readInt();
		for(int i = 0; i < len; i++){
			CoreHash key = CoreHash.read(in);

			int count = in.readInt();
			List<Block> blocks = new ArrayList<Block>(count);
			for(int c = 0; c < count; c++){
				blocks.add(blockMap.get(in.readInt()));
			}

			coreToBlock.put(key, blocks);
		}
	}
	
	/**
	 * Sets the maximum number of same level CPQ intersections allowed.
	 * Limiting intersection CPQs greatly decreases the number of cores that need to be computed.
	 * Note that this limit does not count intersection with identity.
	 * @param intersections The maximum number of CPQs in an intersection.
	 * @throws IllegalStateException When cores have already been computed for this index.
	 */
	public final void setIntersections(int intersections) throws IllegalStateException{
		if(computeCores){
			throw new IllegalStateException("Cores have already been computed.");
		}
		
		maxIntersections = intersections;
	}

	/**
	 * Gets the maximum number of same level CPQ intersections allowed.
	 * Note that this limit does not count intersection with identity.
	 * @return The maximum number of CPQs in an intersection.
	 */
	public final int getIntersections(){
		return maxIntersections;
	}

	/**
	 * Gets the value of k (the CPQ diameter) this index was computed for.
	 * @return The k value for this index.
	 */
	public final int getK(){
		return k;
	}
	
	/**
	 * Write this index to the given output stream.
	 * @param target The output stream to write to.
	 * @param full When true extra information is saved that
	 *        is required for core computation.
	 * @throws IOException When an IOException occurs.
	 * @see #computeCores(int)
	 */
	public final void write(OutputStream target, boolean full) throws IOException{
		DataOutputStream out = new DataOutputStream(target);
		out.writeBoolean(full);
		out.writeBoolean(computeCores);
		out.writeBoolean(computeLabels);
		out.writeInt(maxIntersections);
		out.writeInt(k);
		
		if(full){
			out.writeInt(predicates.size());
			for(Predicate p : predicates){
				byte[] str = p.getAlias().getBytes(StandardCharsets.UTF_8);
				out.writeInt(str.length);
				out.write(str);
			}
		}
		
		out.writeInt(layers.get(k - 1).stream().mapToInt(Block::getId).max().orElse(0) + 1);
		for(int i = full ? 0 : (k - 1); i < k; i++){
			List<Block> layer = layers.get(i);
			out.writeInt(layer.size());
			for(Block block : layer){
				block.write(out, full);
			}
		}
		
		out.writeInt(coreToBlock.size());
		for(Entry<CoreHash, List<Block>> entry : coreToBlock.entrySet()){
			entry.getKey().write(out);
			List<Block> blocks = entry.getValue();
			out.writeInt(blocks.size());
			for(Block block : blocks){
				out.writeInt(block.getId());
			}
		}
	}
	
	/**
	 * Runs the given query on this index and returns the result. Note that
	 * the intersection limit has to be respected if a limit was set.
	 * @param cpq The query to run.
	 * @return The paths matched by the query.
	 * @throws IllegalArgumentException When the query has a diameter equal
	 *         to 0 or larger than the diameter of this index.
	 * @see #setIntersections(int)
	 * @see CPQ#getDiameter()
	 * @see #computeResultCardinality(CPQ)
	 */
	public final List<Pair> query(CPQ cpq) throws IllegalArgumentException{
		return streamBlocks(cpq).flatMap(b->b.getPaths().stream()).toList();
	}

	/**
	 * Computes the number of paths matched by the given query.
	 * Note that the intersection limit has to be respected if a limit was set.
	 * @param cpq The query to compute the number of paths for.
	 * @return The number of paths matched by the query.
	 * @throws IllegalArgumentException When the query has a diameter equal
	 *         to 0 or larger than the diameter of this index.
	 * @see #setIntersections(int)
	 * @see CPQ#getDiameter()
	 * @see #query(CPQ)
	 */
	public final long computeResultCardinality(CPQ cpq) throws IllegalArgumentException{
		return streamBlocks(cpq).mapToLong(Block::getPathCount).sum();
	}
	
	/**
	 * Returns a stream over the blocks matched by the given query.
	 * Note that the intersection limit has to be respected if a limit was set.
	 * @param cpq The query to find blocks for.
	 * @return A stream over the blocks matched by the given query.
	 * @throws IllegalArgumentException When the query has a diameter equal
	 *         to 0 or larger than the diameter of this index.
	 * @see #setIntersections(int)
	 */
	private final Stream<Block> streamBlocks(CPQ cpq) throws IllegalArgumentException{
		if(cpq.getDiameter() > k || cpq.getDiameter() == 0){
			throw new IllegalArgumentException("Query diameter equal to 0 or larger than index diameter.");
		}
		
		return coreToBlock.getOrDefault(
			CanonForm.computeCanon(cpq, false).toHashCanon(),
			Collections.emptyList()
		).stream();
	}
	
	/**
	 * Sets the progress listener to send computation updates to.
	 * @param listener The listener to send computation updates to.
	 * @see ProgressListener
	 */
	public final void setProgressListener(ProgressListener listener){
		progress = listener;
	}
	
	/**
	 * Gets all the blocks in this index.
	 * @return All the blocks in this index.
	 */
	public final List<Block> getBlocks(){
		return blocks;
	}
	
	/**
	 * Sorts all the list of blocks of this index. There's is no real
	 * reason to do this other than to make the output of {@link #print()}
	 * more organised.
	 * @see #print()
	 */
	public final void sort(){
		for(Block block : blocks){
			block.paths.sort(null);
			if(block.labels != null){
				block.labels.sort(null);
			}
		}
		
		blocks.sort(Comparator.comparing(b->b.paths.get(0)));
	}
	
	/**
	 * Prints the index to standard output.
	 * @see #sort()
	 */
	public final void print(){
		int maxPath = blocks.stream().mapToInt(b->b.paths.size()).max().orElse(0);
		int maxLab = blocks.stream().mapToInt(b->b.labels.size()).max().orElse(0);
		StringBuilder[] out = new StringBuilder[4 + maxPath + maxLab + blocks.stream().mapToInt(b->b.cores.size()).max().orElse(0)];
		int labStart = 3 + blocks.stream().mapToInt(b->b.paths.size()).max().orElse(0);
		int coreStart = labStart + 1 + maxLab;
		for(int i = 0; i < out.length; i++){
			out[i] = new StringBuilder();
		}
		
		int currentWidth = 0;
		for(Block block : blocks){
			int blockWidth = 5;
			if(currentWidth > 0){
				for(int i = 0; i < out.length; i++){
					out[i].append('|');
				}
			}
			
			out[0].append(block.id);
			for(int i = 0; i < block.paths.size(); i++){
				String str = block.paths.get(i).toString();
				out[i + 2].append(str);
				blockWidth = Math.max(blockWidth, str.length());
			}
			
			out[labStart - 1].append("-----");
			for(int i = 0; i < block.labels.size(); i++){
				String str = block.labels.get(i).toString();
				out[labStart + i].append(str);
				blockWidth = Math.max(blockWidth, str.length());
			}
			
			out[coreStart - 1].append("-----");
			for(int i = 0; i < block.cores.size(); i++){
				String str = block.cores.get(i).toString();
				out[coreStart + i].append(str);
				blockWidth = Math.max(blockWidth, str.length());
			}
			
			currentWidth += blockWidth + 2;
			for(int i = 0; i < out.length; i++){
				while(out[i].length() < currentWidth){
					out[i].append((out[i].length() == 0 || out[i].charAt(out[i].length() - 1) != '-') ? ' ' : '-');
				}
			}
		}
		
		for(StringBuilder buf : out){
			System.out.println(buf.toString());
		}
	}
	
	/**
	 * Constructs the map from CPQ core has to the blocks this core occurs in.
	 */
	private final void mapCoresToBlocks(){
		progress.mapStart();
		for(Block block : blocks){
			for(CoreHash core : block.canonCores){
				coreToBlock.computeIfAbsent(core, _->new ArrayList<Block>()).add(block);
			}
			
			if(!computeLabels){
				block.canonCores = null;
			}
		}
		progress.mapEnd();
	}
	
	/**
	 * Gets the total number of cores in this index, this is the sum
	 * of all cores in each block.
	 * @return The total number of cores.
	 * @see #getUniqueCores()
	 */
	public final long getTotalCores(){
		return coreToBlock.values().stream().mapToInt(List::size).summaryStatistics().getSum();
	}
	
	/**
	 * Gets the total number of unique cores in this index, this is
	 * the number of unique index keys.
	 * @return The total number of unique cores.
	 * @see #getTotalCores()
	 */
	public final int getUniqueCores(){
		return coreToBlock.size();
	}
	
	/**
	 * After graph partitioning computes the index blocks.
	 * @param segments The partitioned segments of the graph.
	 * @see #partition(UniqueGraph)
	 */
	private final void computeBlocks(RangeList<List<LabelledPath>> segments){
		Map<Pair, LabelledPath> unused = new HashMap<Pair, LabelledPath>();
		
		for(int j = 0; j < k; j++){
			final int lk = j + 1;
			progress.computeBlocksStart(lk);

			List<Block> layerBlocks = layers.get(j);
			List<LabelledPath> segs = segments.get(j);
			int start = 0;
			int lastId = segs.get(0).getSegmentId();
			for(int i = 0; i <= segs.size(); i++){
				if(i == segs.size() || segs.get(i).getSegmentId() != lastId){
					List<LabelledPath> slice = segs.subList(start, i);
					layerBlocks.add(new Block(lk, slice));
					
					if(lk != k){
						for(LabelledPath path : slice){
							unused.put(path.getPair(), path);
						}
					}else{
						for(LabelledPath path : slice){
							unused.remove(path.getPair());
						}
					}
					
					if(i != segs.size()){
						lastId = segs.get(i).getSegmentId();
						start = i;
					}
				}
			}
			
			if(lk != k){
				progress.computeBlocksEnd(lk);
			}
		}
		
		//any remaining pairs denote blocks from previous layers
		List<LabelledPath> remaining = unused.values().stream().sorted(Comparator.comparing(LabelledPath::getSegmentId)).collect(Collectors.toList());
		if(!remaining.isEmpty()){
			int start = 0;
			int lastId = remaining.get(0).getSegmentId();
			for(int i = 0; i <= remaining.size(); i++){
				if(i == remaining.size() || remaining.get(i).getSegmentId() != lastId){
					List<LabelledPath> slice = remaining.subList(start, i);
					blocks.add(new Block(k, slice));
					
					if(i != remaining.size()){
						lastId = remaining.get(i).getSegmentId();
						start = i;
					}
				}
			}
		}
		
		progress.computeBlocksEnd(k);
	}
	
	/**
	 * Computes CPQ cores for each block in this index. Note that if this index
	 * was saved and read back that it is only possible to compute cores if the
	 * index was fully saved with extra state information.
	 * @param threads The number of CPU threads to use to compute cores.
	 * @throws InterruptedException When the current thread is interrupted.
	 * @throws IllegalStateException When cores have already been computed for
	 *         this index of when this index is read back and was not fully saved.
	 * @see #setIntersections(int)
	 * @see #setProgressListener(ProgressListener)
	 * @see #write(OutputStream, boolean)
	 * @see #Index(InputStream)
	 */
	public final void computeCores(int threads) throws InterruptedException, IllegalStateException{
		if(computeCores){
			throw new IllegalStateException("Cores have already been computed.");
		}else if(predicates == null){
			throw new IllegalStateException("Cannot compute cores on an index that wasn't fully saved.");
		}
		
		ExecutorService executor = Executors.newFixedThreadPool(threads);

		//process cores layer by layer
		for(int i = 0; i < k; i++){
			progress.coresStart(i + 1);
			
			final int total = layers.get(i).size();
			Lock lock = new ReentrantLock();
			Condition cond = lock.newCondition();
			AtomicInteger done = new AtomicInteger(0);
			ListIterator<Block> iter = layers.get(i).listIterator(total);
			while(iter.hasPrevious()){
				Block block = iter.previous();
				executor.execute(()->{
					try{
						block.computeCores();

						if(done.incrementAndGet() == total){
							lock.lock();
						}else if(!lock.tryLock()){
							return;
						}

						try{
							cond.signal();
						}finally{
							lock.unlock();
						}
					}catch(Throwable t){
						System.err.println("FATAL");
						t.printStackTrace();
						progress.intermediateProgress(-1, -1, -1);
					}
				});
			}
			
			long lastUpdate = 0;
			while(true){
				try{
					lock.lock();
					if(cond.await(10, TimeUnit.MINUTES)){
						int val = done.get();
						progress.coresBlocksDone(val, total);
						if(val == total){
							break;
						}
					}

					if(lastUpdate < System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(10)){
						progress.intermediateProgress(blocks.stream().mapToInt(b->b.canonCores.size()).summaryStatistics().getSum(), done.get(), total);
						lastUpdate = System.currentTimeMillis();
					}
				}finally{
					lock.unlock();
				}
			}
			
			progress.coresEnd(i + 1);
		}
		
		executor.shutdown();
		computeCores = true;
		mapCoresToBlocks();
	}
	
	/**
	 * Partitions all the paths in the given graph according to k-path-bisimulation.
	 * @param g The graph to partition.
	 * @return The partitioned paths in the graph.
	 * @throws IllegalArgumentException When the diameter of this index k is less than 1.
	 */
	private final RangeList<List<LabelledPath>> partition(UniqueGraph<Integer, Predicate> g) throws IllegalArgumentException{
		if(k <= 0){
			throw new IllegalArgumentException("Invalid value of k for bisimulation, has to be 1 or greater.");
		}
		
		progress.partitionStart(1);
		RangeList<List<LabelledPath>> segments = new RangeList<List<LabelledPath>>(k, ArrayList::new);
		Map<Pair, LabelledPath> history = new HashMap<Pair, LabelledPath>();
		int vertexCount = g.getNodeCount();
		RangeList<RangeList<List<LabelledPath>>> adjacencyByLayer = new RangeList<RangeList<List<LabelledPath>>>(k);
		
		//classes for 1-path-bisimulation
		Map<Pair, LabelledPath> pathMap = new HashMap<Pair, LabelledPath>();
		predicates = new RangeList<Predicate>(1 + g.getEdges().stream().mapToInt(e->e.getData().getID()).max().orElse(0));
		for(GraphEdge<Integer, Predicate> edge : g.getEdges()){
			//forward and backward edges are just the labels on those edges
			LabelledPath path = pathMap.computeIfAbsent(new Pair(edge.getSource(), edge.getTarget()), p->new LabelledPath(p, null));
			path.addLabel(edge.getData());
			history.put(path.getPair(), path);
			
			path = pathMap.computeIfAbsent(new Pair(edge.getTarget(), edge.getSource()), p->new LabelledPath(p, null));
			path.addLabel(edge.getData().getInverse());
			history.put(path.getPair(), path);
			
			predicates.set(edge.getData(), edge.getData());
		}
		
		//sort 1-path
		List<LabelledPath> segOne = segments.get(0);
		pathMap.values().stream().sorted(Index::sortOnePath).forEachOrdered(segOne::add);
		
		//assign block IDs
		LabelledPath prev = null;
		int id = 1;
		for(LabelledPath seg : segOne){
			if(prev != null && (!seg.equalLabels(prev) || seg.isLoop() ^ prev.isLoop())){
				//if labels and cyclic patterns (loop) are not the same a new ID is started
				id++;
			}

			seg.setSegmentId(id);
			prev = seg;
		}
		adjacencyByLayer.set(0, buildAdjacencyMapping(segOne, vertexCount));
		progress.partitionEnd(1);
		
		//classes for 2-path-bisimulation to k-path-bisimulation
		for(int i = 1; i < k; i++){
			progress.partitionStart(i + 1);
			pathMap.clear();

			id++;
			for(int k1 = i - 1; k1 >= 0; k1--){//all combinations to make CPQi
				int k2 = i - k1 - 1;
				RangeList<List<LabelledPath>> endMapping = adjacencyByLayer.get(k2);
				
				progress.partitionCombinationStart(k1 + 1, k2 + 1);
				for(LabelledPath seg : segments.get(k1)){
					List<LabelledPath> endMatches = endMapping.get(seg.getTarget());
					if(endMatches == null){
						continue;
					}
					
					for(LabelledPath end : endMatches){
						
						Pair key = new Pair(seg.getSource(), end.getTarget());
						LabelledPath path = pathMap.computeIfAbsent(key, p->{
							LabelledPath newPath = new LabelledPath(p, history.get(p));
							history.put(p, newPath);
							return newPath;
						});
						
						path.addSegment(seg, end);
						if(k2 == 0 && computeLabels){//slight optimisation, since we only need one combination to find all paths
							for(LabelSequence labels : seg.getLabels()){
								for(LabelSequence label : end.getLabels()){
									path.addLabel(labels, label);
								}
							}
						}
					}
				}
				
				progress.partitionCombinationEnd(k1 + 1, k2 + 1);
			}
			
			//sort
			List<LabelledPath> segs = segments.get(i);
			pathMap.values().forEach(LabelledPath::cacheHashCode);
			pathMap.values().stream().sorted(Index::sortPaths).forEachOrdered(segs::add);

			//assign IDs
			prev = null;
			for(LabelledPath path : segs){
				if(prev != null && (path.compareSegmentsTo(prev) != 0 || prev.isLoop() ^ path.isLoop())){
					//increase id if loop status or segments differ
					id++;
				}

				path.setSegmentId(id);
				prev = path;
			}
			
			adjacencyByLayer.set(i, buildAdjacencyMapping(segs, vertexCount));
			progress.partitionEnd(i + 1);
		}
		
		return segments;
	}
	
	/**
	 * Builds an adjacency mapping from each source vertex to the segments that start there.
	 * This enables a mapped join when combining segments.
	 * @param segments The segments to map.
	 * @param vertexCount The total number of vertices in the graph.
	 * @return The adjacency mapping by source vertex.
	 */
	private static RangeList<List<LabelledPath>> buildAdjacencyMapping(List<LabelledPath> segments, int vertexCount){
		RangeList<List<LabelledPath>> adjacency = new RangeList<List<LabelledPath>>(vertexCount);

		for(LabelledPath segment : segments){
			List<LabelledPath> adj = adjacency.get(segment.getSource());
			if(adj == null){
				adj = new ArrayList<LabelledPath>();
				adjacency.set(segment.getSource(), adj);
			}
			
			adj.add(segment);
		}
		
		return adjacency;
	}
	
	/**
	 * Compares the given paths based on their segments,
	 * cyclic properties, source and target.
	 * @param a The first path.
	 * @param b The second path.
	 * @return A value less than 0 if {@code a < b}, a value equal
	 *         to 0 if {@code a == b} and a value greater than 0 if
	 *         {@code a > b}.
	 */
	private static final int sortPaths(LabelledPath a, LabelledPath b){
		int cmp = a.compareSegmentsTo(b);
		if(cmp != 0){
			return cmp;
		}

		cmp = Boolean.compare(a.isLoop(), b.isLoop());
		if(cmp != 0){
			return cmp;
		}

		return a.comparePathTo(b);
	}
	
	/**
	 * Compares the given paths based on their labels,
	 * cyclic properties, source and target.
	 * @param a The first path.
	 * @param b The second path.
	 * @return A value less than 0 if {@code a < b}, a value equal
	 *         to 0 if {@code a == b} and a value greater than 0 if
	 *         {@code a > b}.
	 */
	private static final int sortOnePath(LabelledPath a, LabelledPath b){
		int cmp = a.compareLabelsTo(b);
		if(cmp != 0){
			return cmp;
		}
		
		cmp = Boolean.compare(a.isLoop(), b.isLoop());
		if(cmp != 0){
			return cmp;
		}

		return a.comparePathTo(b);
	}
	
	/**
	 * Representation of a single block in the index containing
	 * the paths, labels and cores of the partition it represents.
	 * @author Roan
	 */
	public final class Block{
		/**
		 * The ID of this block.
		 */
		private final int id;
		/**
		 * The value of k for the index layer this core belongs to.
		 */
		private final int k;
		/**
		 * A list of all paths stored at this block.
		 */
		private final List<Pair> paths;
		/**
		 * A list of all label sequences that map to this block. This is
		 * the same set of label sequences as computed in the original
		 * paper on language aware indexing. This list may also be set
		 * to null if its computation is not explicitly requested by
		 * setting {@link Index#computeLabels} to true.
		 */
		private List<LabelSequence> labels;
		/**
		 * Explicit core informations for cores stored in this block.
		 * This list is never restored for an index that was saved and
		 * read back and is also cleared after core computation unless
		 * saving labels is enabled.
		 * @see Index#computeLabels
		 */
		private List<CPQ> cores;
		/**
		 * Hashes for the cores in this index block. Explicit forms are
		 * optionally stored in {@link #cores}.
		 */
		private Set<CoreHash> canonCores;
		/**
		 * Blocks from previous layers that were combined to form this layer.
		 */
		private List<BlockPair> combinations;
		/**
		 * The block from the previous layer that the paths in this block were stored at.
		 * @see #paths
		 */
		private Block ancestor;
		
		/**
		 * Constructs a new index block for the given diameter and with the given paths.
		 * @param k The diameter this block is for, corresponds to the index layer.
		 * @param slice The paths to store at this block.
		 */
		private Block(int k, List<LabelledPath> slice){
			this.k = k;
			
			LabelledPath range = slice.get(0);
			id = range.getSegmentId();
			paths = slice.stream().map(LabelledPath::getPair).collect(Collectors.toList());
			slice.forEach(s->s.setBlock(this));
			combinations = range.getSegments().stream().map(BlockPair::new).toList();
			cores = new ArrayList<CPQ>();
			canonCores = new HashSet<CoreHash>();
			
			if(computeLabels || combinations.isEmpty()){
				//we need labels to compute cores for k = 1 and in rare cases higher k where a k = 1 block did not get any higher k paths added
				labels = new ArrayList<LabelSequence>();
				labels.addAll(range.getLabels());
			}else{
				labels = null;
			}
			
			//we inherit all labels from the previous layer block the paths in this block are a subset of
			if(range.hasAncestor()){
				ancestor = range.getAncestor().getBlock();
				if(computeLabels){
					labels.addAll(ancestor.labels);
				}
			}else{
				ancestor = null;
			}
		}
		
		/**
		 * Reads a previously saved block from the given input stream.
		 * @param in The stream to read from.
		 * @param full True if extra information has to be read.
		 * @param blockMap A map of already read blocks indexed by ID.
		 * @throws IOException When an IOException occurs.
		 */
		private Block(DataInputStream in, boolean full, RangeList<Block> blockMap) throws IOException{
			id = in.readInt();
			
			int len = in.readInt();
			paths = new ArrayList<Pair>(len);
			for(int i = 0; i < len; i++){
				paths.add(new Pair(in));
			}
			
			if(full){
				k = in.readInt();
				
				len = in.readInt();
				labels = new ArrayList<LabelSequence>(len);
				for(int i = 0; i < len; i++){
					labels.add(new LabelSequence(in, predicates));
				}
				
				int anc = in.readInt();
				ancestor = anc == -1 ? null : blockMap.get(anc);
				
				len = in.readInt();
				combinations = new ArrayList<BlockPair>(len);
				for(int i = 0; i < len; i++){
					combinations.add(new BlockPair(in, blockMap));
				}
				
				len = in.readInt();
				canonCores = new HashSet<CoreHash>(len);
				for(int i = 0; i < len; i++){
					canonCores.add(CoreHash.read(in));
				}
				
				cores = new ArrayList<CPQ>();
			}else{
				k = -1;
				ancestor = null;
				labels = null;
				combinations = null;
				canonCores = null;
				cores = null;
			}
		}
		
		/**
		 * Writes this block to the given stream.
		 * @param out The stream to write to.
		 * @param full True to write extended information required
		 *        to later compute cores.
		 * @throws IOException When an IOException occurs.
		 */
		private final void write(DataOutputStream out, boolean full) throws IOException{
			out.writeInt(id);
			
			out.writeInt(paths.size());
			for(Pair pair : paths){
				pair.write(out);
			}
			
			if(full){
				out.writeInt(k);
				
				out.writeInt(labels == null ? 0 : labels.size());
				if(labels != null){
					for(LabelSequence seq : labels){
						seq.write(out);
					}
				}
				
				out.writeInt(ancestor == null ? -1 : ancestor.getId());
				
				out.writeInt(combinations == null ? 0 : combinations.size());
				if(combinations != null){
					for(BlockPair pair : combinations){
						pair.write(out);
					}
				}
				
				out.writeInt(canonCores == null ? 0 : canonCores.size());
				if(canonCores != null){
					for(CoreHash core : canonCores){
						core.write(out);
					}
				}
			}
		}

		/**
		 * Gets the ID of this block. This is equal to
		 * the ID of the segments this block was built from.
		 * @return The ID of this block.
		 */
		public final int getId(){
			return id;
		}
		
		/**
		 * Gets the paths stored at this block.
		 * @return The paths for this block.
		 */
		public final List<Pair> getPaths(){
			return paths;
		}

		/**
		 * Gets the number of paths stored at this block.
		 * @return The number of paths for this block.
		 */
		public final int getPathCount(){
			return paths.size();
		}
		
		/**
		 * Gets the label sequences that map to this block.
		 * @return The label sequences that map to this block.
		 *         This value may be null unless label computation
		 *         was explicitly requested via {@link Index#computeLabels}.
		 */
		public final List<LabelSequence> getLabels(){
			return labels;
		}
		
		/**
		 * Gets the hashes of the cores that map to this block.
		 * @return The cores for this block.
		 *         This value may be null unless label computation
		 *         was explicitly requested via {@link Index#computeLabels}.
		 */
		public final Set<CoreHash> getCanonCores(){
			return canonCores;
		}
		
		/**
		 * Gets the cores that map to this block.
		 * @return The cores that map to this block.
		 *         This value may be null unless label computation
		 *         was explicitly requested via {@link Index#computeLabels}.
		 */
		public final List<CPQ> getCores(){
			return cores;
		}
		
		/**
		 * Checks if this block represents a loop, this means that
		 * all paths in this block have the same source and target vertex.
		 * @return True if this block represents a loop.
		 */
		public final boolean isLoop(){
			return paths.get(0).isLoop();
		}
		
		/**
		 * Adds a new core to this index.
		 * @param canon The canonical form of the core to add.
		 * @param noSave True if the explicit form of this core
		 *        does not need to be saved to {@link #cores}.
		 */
		private final void addCore(CanonForm canon, boolean noSave){
			if(canonCores.add(canon.toHashCanon())){
				if(!noSave){
					cores.add(canon.getCPQ());
				}
			}
		}
		
		/**
		 * Adds a new core to this index.
		 * @param q The CPQ to add, the core of this CPQ
		 *        is always computed first before adding.
		 * @param noSave True if the explicit form of this core
		 *        does not need to be saved to {@link #cores}.
		 */
		private final void addCore(CPQ q, boolean noSave){
			addCore(CanonForm.computeCanon(q, false), noSave);
		}
		
		/**
		 * Computes all the CPQ cores for this block.
		 */
		private final void computeCores(){
			//inherited from previous layer blocks
			if(ancestor != null){//only need to go back one level since the previous level already collected the level before that
				//these are by definition of a different diameter
				canonCores.addAll(ancestor.canonCores);
				cores.addAll(ancestor.cores);
			}
			
			//all cores so far are inherited fully processed cores from the ancestor, we skip these for intersection with each other and identity
			final int skip = cores.size();
			boolean noSave = k == Index.this.k && !computeLabels;
			
			if(combinations.isEmpty()){
				//for layer 1 the cores are the label sequences (which are distinct cores)
				labels.stream().map(LabelSequence::getLabels).map(CPQ::labels).map(q->CanonForm.computeCanon(q, true)).forEach(c->this.addCore(c, false));
			}else{
				//all combinations of cores from previous layers (this can generate duplicates, but all are cores unless both cores are a loop)
				for(BlockPair pair : combinations){
					for(CPQ core1 : pair.first().cores){
						for(CPQ core2 : pair.second().cores){
							addCore(CPQ.concat(core1, core2), false);
						}
					}
				}
			}
			
			//all cores up to intersection
			final int end = cores.size();
			
			//all intersections of cores (these are all distinct cores unless blackflow happens, so we cannot assume them to be cores)
			if(maxIntersections >= 2){
				QueryGraphCPQ[] graphs = new QueryGraphCPQ[cores.size()];
				for(int i = 0; i < graphs.length; i++){
					graphs[i] = cores.get(i).toQueryGraph();
				}

				final int max = cores.size();
				BitSet[] conflicts = new BitSet[max];
				conflicts[0] = new BitSet(0);
				for(int i = 1; i < max; i++){
					QueryGraphCPQ a = graphs[i];
					conflicts[i] = new BitSet(i);
					for(int j = skip; j < i; j++){
						//if CPQs are homomorphic they collapse on intersection
						QueryGraphCPQ b = graphs[j];
						if(a.isHomomorphicTo(b) || b.isHomomorphicTo(a)){
							conflicts[i].set(j);
						}
					}
				}
			
				List<CanonForm> held = new ArrayList<CanonForm>();
				for(int i = skip; i < max; i++){
					for(int j = 0; j < i; j++){
						if(!conflicts[i].get(j)){
							//this really only applies for k > 2, but any decrease in options is welcome
							CPQ q = CPQ.intersect(cores.get(i), cores.get(j));
							CanonForm canon = CanonForm.computeCanon(q, false);
							held.add(canon);
							if(canon.wasCore()){
								if(isLoop()){
									held.add(CanonForm.computeCanon(CPQ.intersect(q, CPQ.id()), false));
								}
							}else{
								conflicts[i].set(j);
							}
						}
					}
				}
				
				if(maxIntersections >= 3){
					computeIntersectionCores(cores, 0, skip, max, new ArrayList<CPQ>(), new BitSet(cores.size()), conflicts, noSave, isLoop());
				}
				
				for(CanonForm form : held){
					addCore(form, noSave);
				}
			}
			
			//intersect with identity if possible, these are not always cores and not always unique (note that intersections were already handled so they are skipped)
			if(isLoop()){
				for(int i = skip; i < end; i++){
					addCore(CPQ.intersect(cores.get(i), CPQ.id()), noSave);
				}
			}
			
			if(noSave){
				cores = null;
				labels = null;
				ancestor = null;
				combinations = null;
			}
		}
		
		/**
		 * Computes intersection derived CPQ for this index. All sub sets of the given
		 * list of CPQs need to be intersected and added as a potential core.
		 * @param items The list of CPQs to intersect all sub sets of.
		 * @param offset The current CPQ in the list of CPQs to pick of skip for the
		 *        subset currently being constructed.
		 * @param restricted End of restricted range of CPQs. At most one CPQ from the range
		 *        {@code 0...restricted} is allowed to to be included in an intersection as
		 *        this range includes already computed intersections from a previous layer.
		 * @param max The maximum index in the list of items to pick, higher indices are not considered.
		 * @param set The set of CPQs picked for the current subset.
		 * @param selected Bit set indicating by index which CPQs are picked for the current subset.
		 * @param conflicts Bit set array indicating which CPQs are subsets of each other and thus would
		 *        never be a core if intersected.
		 * @param noSave Whether explicit cores should be saved to {@link #cores}.
		 * @param id True if this block is a loop so all computed cores also need to be intersected with identity.
		 */
		private final void computeIntersectionCores(List<CPQ> items, int offset, final int restricted, final int max, List<CPQ> set, BitSet selected, BitSet[] conflicts, final boolean noSave, final boolean id){
			if(offset >= max || set.size() == maxIntersections){
				if(set.size() >= 3){
					CPQ q = CPQ.intersect(new ArrayList<CPQ>(set));
					CanonForm canon = CanonForm.computeCanon(q, false);
					addCore(canon, noSave);
					if(id && canon.wasCore()){
						addCore(CPQ.intersect(q, CPQ.id()), noSave);
					}
				}
			}else{
				//don't pick the element
				computeIntersectionCores(items, offset + 1, restricted, max, set, selected, conflicts, noSave, id);
				
				//pick the element
				if(conflicts[offset].intersects(selected)){
					//can't pick a conflicting item
					return;
				}
				
				selected.set(offset);
				CPQ q = items.get(offset);
				set.add(q);
				computeIntersectionCores(items, offset < restricted ? restricted : (offset + 1), restricted, max, set, selected, conflicts, noSave, id);
				set.remove(set.size() - 1);
				selected.clear(offset);
			}
		}
		
		@Override
		public String toString(){
			StringBuilder builder = new StringBuilder();
			builder.append("Block[id=");
			builder.append(id);
			builder.append(",paths=");
			builder.append(paths);
			builder.append(",labels={");
			if(labels != null){
				for(LabelSequence seq : labels){
					builder.append(seq.toString());
					builder.append(",");
				}
				builder.delete(builder.length() - 1, builder.length());
			}
			builder.append("},cores={");
			if(cores != null){
				for(CPQ core : cores){
					builder.append(core.toString());
					builder.append(",");
				}
				if(!cores.isEmpty()){
					builder.delete(builder.length() - 1, builder.length());
				}
			}
			builder.append("}]");
			return builder.toString();
		}
	}
}
