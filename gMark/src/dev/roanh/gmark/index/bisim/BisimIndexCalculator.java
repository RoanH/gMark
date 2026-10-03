package dev.roanh.gmark.index.bisim;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.roanh.cpqindex.CanonForm;
import dev.roanh.cpqindex.LabelSequence;
import dev.roanh.cpqindex.LabelledPath;
import dev.roanh.cpqindex.Pair;
import dev.roanh.gmark.type.schema.Predicate;
import dev.roanh.gmark.util.RangeList;
import dev.roanh.gmark.util.graph.generic.UniqueGraph;
import dev.roanh.gmark.util.graph.generic.UniqueGraph.GraphEdge;

public class BisimIndexCalculator{
	/**
	 * The value of k (the CPQ diameter) to compute the index for.
	 */
	private int k;//default 0 (invalid)
//	/**
//	 * Boolean indicating whether explicit representations of cores
//	 * and label sequences should be saved for the computed blocks.
//	 * Saving these will take significantly more memory and is only
//	 * really relevant when printing the index with {@link #print()},
//	 * calling {@link Block#getCores()} or calling {@link Block#getLabels()}.
//	 */
	@Deprecated//? full=yes, otherwise=no? should work
	private boolean computeLabels;//default false
	private boolean sorted;
	/**
	 * Progress listener to inform of any computation updates.
	 */
	private BisimProgressListener progress = BisimProgressListener.NONE;
	
	public void setComputeLabels(boolean computeLabels){
		this.computeLabels = computeLabels;
	}
	
	public void setK(int k){
		this.k = k;
	}
	
	public void setSorted(boolean sorted){
		this.sorted = sorted;
	}
	
	public void setProgressListener(BisimProgressListener listener){
		progress = listener;
	}

	public BisimIndexFull computeFull(UniqueGraph<Integer, Predicate> g) throws IllegalArgumentException{
		IndexData data = computeLayers(g);
		return new BisimIndexFull(k, data.layers(), data.predicates());
	}
	
//	 * @throws IllegalArgumentException When the diameter of this index k is less than 1 or too many labels are in the graph.

	public BisimIndex compute(UniqueGraph<Integer, Predicate> g) throws IllegalArgumentException{
		IndexData data = computeLayers(g);
		return new BisimIndex(k, data.layers().get(k - 1), data.predicates());
	}
	
	private IndexData computeLayers(UniqueGraph<Integer, Predicate> g) throws IllegalArgumentException{
		if(k <= 0){
			throw new IllegalArgumentException("Invalid value of k for bisimulation, has to be 1 or greater.");
		}
		
		RangeList<Predicate> predicates = new RangeList<Predicate>(1 + g.getEdges().stream().mapToInt(e->e.getData().getID()).max().orElse(0));
		if(Math.powExact(2, CanonForm.MAX_LABEL_BITS) < predicates.size()){
			throw new IllegalArgumentException("More labels in the input graph than supported.");
		}
		
		RangeList<List<BisimBlock>> layers = computeBlocks(partition(g, predicates));
		if(sorted){
			sort(layers.get(k - 1));
		}
		
		return new IndexData(layers, predicates);
	}
	
	/**
	 * Partitions all the paths in the given graph according to k-path-bisimulation.
	 * @param g The graph to partition.
	 * @param predicates List of predicates (labels) that appear in the graph by ID.
	 * @return The partitioned paths in the graph.
	 */
	private final RangeList<List<LabelledPath>> partition(UniqueGraph<Integer, Predicate> g, RangeList<Predicate> predicates){
		progress.partitionStart(1);
		RangeList<List<LabelledPath>> segments = new RangeList<List<LabelledPath>>(k, ArrayList::new);
		Map<Pair, LabelledPath> history = new HashMap<Pair, LabelledPath>();
		int vertexCount = g.getNodeCount();
		RangeList<RangeList<List<LabelledPath>>> adjacencyByLayer = new RangeList<RangeList<List<LabelledPath>>>(k);
		
		//classes for 1-path-bisimulation
		Map<Pair, LabelledPath> pathMap = new HashMap<Pair, LabelledPath>();
		
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
		pathMap.values().stream().sorted(BisimIndexCalculator::sortOnePath).forEachOrdered(segOne::add);
		
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
			pathMap.values().stream().sorted(BisimIndexCalculator::sortPaths).forEachOrdered(segs::add);

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
	 * After graph partitioning computes the index blocks.
	 * @param segments The partitioned segments of the graph.
	 * @see #partition(UniqueGraph)
	 */
	private final RangeList<List<BisimBlock>> computeBlocks(RangeList<List<LabelledPath>> segments){
		Map<Pair, LabelledPath> unused = new HashMap<Pair, LabelledPath>();
		RangeList<List<BisimBlock>> layers = new RangeList<List<BisimBlock>>(k, ArrayList::new);
		
		for(int j = 0; j < k; j++){
			final int lk = j + 1;
			progress.computeBlocksStart(lk);

			List<BisimBlock> layerBlocks = layers.get(j);
			List<LabelledPath> segs = segments.get(j);
			int start = 0;
			int lastId = segs.get(0).getSegmentId();
			for(int i = 0; i <= segs.size(); i++){
				if(i == segs.size() || segs.get(i).getSegmentId() != lastId){
					List<LabelledPath> slice = segs.subList(start, i);
					layerBlocks.add(new BisimBlock(lk, computeLabels, slice));
					
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
		List<LabelledPath> remaining = unused.values().stream().sorted(Comparator.comparing(LabelledPath::getSegmentId)).toList();
		List<BisimBlock> lastLayerBlocks = layers.get(k - 1);
		if(!remaining.isEmpty()){
			int start = 0;
			int lastId = remaining.get(0).getSegmentId();
			for(int i = 0; i <= remaining.size(); i++){
				if(i == remaining.size() || remaining.get(i).getSegmentId() != lastId){
					List<LabelledPath> slice = remaining.subList(start, i);
					lastLayerBlocks.add(new BisimBlock(k, computeLabels, slice));
					
					if(i != remaining.size()){
						lastId = remaining.get(i).getSegmentId();
						start = i;
					}
				}
			}
		}
		
		progress.computeBlocksEnd(k);
		return layers;
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
	
//	/**
//	 * Sorts all the list of blocks of this index. There's is no real
//	 * reason to do this other than to make the output of {@link #print()}
//	 * more organised or deterministic.
//	 * @see #print()
//	 */
	private static final void sort(List<BisimBlock> blocks){
		for(BisimBlock block : blocks){
			block.getPaths().sort(null);
			if(block.getLabels() != null){
				block.getLabels().sort(null);
			}
		}
		
		blocks.sort(Comparator.comparing(b->b.getPaths().get(0)));
	}
	
	private static record IndexData(RangeList<List<BisimBlock>> layers, RangeList<Predicate> predicates){
	}
}
