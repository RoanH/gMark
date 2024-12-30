package dev.roanh.gmark.eval;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Deque;
import java.util.List;

import dev.roanh.gmark.data.CardStat;
import dev.roanh.gmark.data.SourceTargetPair;
import dev.roanh.gmark.util.SmartBitSet;

/**
 * Result graph describing the result of a database operation
 * or query evaluation as a whole. This is an abstract graph
 * representing the existence of paths without describing these
 * paths exactly. In essence this graph can also be interpreted
 * as a set of source target pairs, each representing the existence
 * of one or more paths between the source and target vertex of the pair.
 * <p>
 * The concrete implementation in this class is based on a compressed sparse row matrix (CSR).
 * In addition a result graph may be sorted (meaning its target vertex ranges are sorted).
 * <p>
 * Finally, note that this graph does not perform any input validations for performance reasons
 * (unless assertions are enabled in the JVM). Notably, operations on this class are only well
 * defined if the vertex count of the input result graph argument is the same as the vertex
 * count of this graph.
 * @author Roan
 * @see CardStat
 * @see SourceTargetPair
 */
public class ResultGraph{
	/**
	 * Factor used to allocate more space for the CSR if there is insufficient capacity.
	 */
	private static final int RESIZE_FACTOR = 3;
	/**
	 * The number of vertices in this result graph.
	 */
	private final int vertexCount;
	/**
	 * True if the CSR target ranges for this result graph are sorted.
	 */
	private boolean sorted;
	/**
	 * The CSR storing the data for this graph.
	 */
	private int[] csr;
	/**
	 * The current write head position in {@link #csr}, i.e., the
	 * next write operation will start at this index.
	 */
	private int head;
	
	/**
	 * Constructs a new result graph with the given properties.
	 * @param vertexCount The number of vertices for the result graph.
	 * @param sizeEstimate The estimated number of edges for the result graph,
	 *        this will allocated at least enough space for the requested number
	 *        of edges, but the graph will still grow as required.
	 * @param sorted True if the data that will be stored in this graph is
	 *        guaranteed to result in a sorted result graph, i.e., this is
	 *        a promise to this result graph that will make it assume sorted data.
	 */
	protected ResultGraph(int vertexCount, int sizeEstimate, boolean sorted){
		this.vertexCount = vertexCount;
		this.sorted = sorted;
		csr = new int[vertexCount + 1 + sizeEstimate];
		head = vertexCount + 1;
	}
	
	/**
	 * Constructs a new result graph that is a copy of the given result graph.
	 * @param base The result graph to copy.
	 * @param sizeEstimate The estimated number of edges for the result graph,
	 *        this will allocated at least enough space for the requested number
	 *        of edges, but the graph will still grow as required. If the given
	 *        based graph is larger than the given estimate, the estimate is ignored.
	 * @param sorted True if the data that will be stored in this graph is
	 *        guaranteed to result in a sorted result graph, i.e., this is
	 *        a promise to this result graph that will make it assume sorted data.
	 */
	private ResultGraph(ResultGraph base, int sizeEstimate, boolean sorted){
		this.sorted = sorted;
		vertexCount = base.vertexCount;
		head = base.head;
		csr = Arrays.copyOf(base.csr, Math.max(base.csr.length, sizeEstimate));
	}
	
	/**
	 * Constructs a new result graph with the given vertex count.
	 * @param vertexCount The vertex count for the result graph.
	 */
	private ResultGraph(int vertexCount){
		this.vertexCount = vertexCount;
		sorted = true;
		csr = new int[vertexCount + 1];
		Arrays.fill(csr, csr.length);
		head = csr.length;
	}
	
	/**
	 * Constructs a new result graph with a single source vertex with outgoing edges.
	 * @param vertexCount The vertex count for the result graph.
	 * @param source The source vertex that has outgoing edges.
	 * @param sorted True if the data that will be stored in this graph is
	 *        guaranteed to result in a sorted result graph, i.e., this is
	 *        a promise to this result graph that will make it assume sorted data.
	 * @param from The start index in targets (inclusive).
	 * @param to The end index in targets (exclusive).
	 * @param targets The target vertices for the given source vertex, contained in the
	 *        range specified by the from and to parameters.
	 */
	private ResultGraph(int vertexCount, int source, boolean sorted, int from, int to, int... targets){
		this.vertexCount = vertexCount;
		this.sorted = sorted;
		csr = new int[vertexCount + 1 + to - from];
		Arrays.fill(csr, 0, source + 1, vertexCount + 1);
		Arrays.fill(csr, source + 1, vertexCount + 1, csr.length);
		System.arraycopy(targets, from, csr, vertexCount + 1, to - from);
		head = csr.length;
	}
	
	/**
	 * Gets the number of edges in this result graph.
	 * @return The number of edges in this result graph.
	 */
	public int getEdgeCount(){
		return csr[vertexCount] - csr[0];
	}
	
	/**
	 * Gets the number of vertices for this result graph.
	 * @return The number of vertices for this result graph.
	 */
	public int getVertexCount(){
		return vertexCount;
	}
	
	/**
	 * Sets the active source vertex for target vertex write operations.
	 * <p>
	 * Note: the new source vertex always has to be the subsequent source
	 * vertex, i.e., if the previous source vertex was 1, the next active
	 * source vertex has to be 2.
	 * @param source The new active source vertex.
	 * @see #addTarget(int)
	 * @see #endFinalSource()
	 */
	public void setActiveSource(int source){
		assert source == 0 || (vertexCount < csr[source - 1] && csr[source - 1] <= head);
		csr[source] = head;
	}
	
	/**
	 * Adds a new target vertex to the active source vertex.
	 * @param target The target vertex to add.
	 * @see #setActiveSource(int)
	 */
	public void addTarget(int target){
		if(head >= csr.length){
			csr = Arrays.copyOf(csr, RESIZE_FACTOR * csr.length);
		}
		
		csr[head++] = target;
	}
	
	/**
	 * Ends target writing for the final source vertex in the result graph.
	 * After this method was called no more calls to {@link #setActiveSource(int)}
	 * and {@link #addTarget(int)} are possible.
	 */
	public void endFinalSource(){
		csr[vertexCount] = head;
	}
	
	/**
	 * Sorts the target ranges for this result graph if they are not yet sorted.
	 * Each target range will be sorted in ascending order.
	 */
	public void sort(){
		if(!sorted){
			for(int source = 0; source < vertexCount; source++){
				Arrays.sort(csr, csr[source], csr[source + 1]);
			}
			
			sorted = true;
		}
	}
	
	/**
	 * Checks if the target ranges for this result graph are sorted.
	 * @return True if all target ranges for this result graph are sorted.
	 * @see #sort()
	 */
	public boolean isSorted(){
		return sorted;
	}
	
	/**
	 * Computes the disjunction (or union) of this graph and the given input graph.
	 * Recall that this operation simply added all the paths in both input graphs
	 * to the result graph. This method also ensures target lists for each vertex
	 * remain (or become) sorted and prevents duplicates from ending up in the output.
	 * <p>
	 * Note: behaviour is undefined if the other result graph has a different vertex count.
	 * @param other The other input graph to compute the union with.
	 * @return The result graph representing the union of this graph and the input graph.
	 */
	public ResultGraph union(ResultGraph other){
		assert vertexCount == other.vertexCount;
		
		sort();
		other.sort();
		ResultGraph out = new ResultGraph(vertexCount, getEdgeCount() + other.getEdgeCount(), true);
		
		for(int source = 0; source < out.vertexCount; source++){
			out.setActiveSource(source);
			
			int li = csr[source];
			final int le = csr[source + 1];
			
			int ri = other.csr[source];
			final int re = other.csr[source + 1];
			
			while(li < le && ri < re){
				final int l = csr[li];
				final int r = other.csr[ri];
				
				if(l == r){
					out.addTarget(l);
					li++;
					ri++;
				}else if(l < r){
					out.addTarget(l);
					li++;
				}else{
					out.addTarget(r);
					ri++;
				}
			}
			
			while(li < le){
				out.addTarget(csr[li++]);
			}
			
			while(ri < re){
				out.addTarget(other.csr[ri++]);
			}
		}
		
		out.endFinalSource();
		return out;
	}
	
	/**
	 * Computes the intersection of this graph and the given input graph.
	 * Recall that this operation simply discards all paths that are not
	 * present in both input graphs. This method also ensures target lists
	 * for each vertex remain (or become) sorted and prevents duplicates
	 * from ending up in the output.
	 * <p>
	 * Note: behaviour is undefined if the other result graph has a different vertex count.
	 * @param other The other input graph to compute the intersection with.
	 * @return The result graph representing the intersection of this graph and the input graph.
	 */
	public ResultGraph intersection(ResultGraph other){
		assert vertexCount == other.vertexCount;
		
		sort();
		other.sort();
		ResultGraph out = new ResultGraph(vertexCount, Math.min(getEdgeCount(), other.getEdgeCount()), true);
		
		for(int source = 0; source < out.vertexCount; source++){
			out.setActiveSource(source);

			final int ls = csr[source];
			final int le = csr[source + 1];
			final int rs = other.csr[source];
			final int re = other.csr[source + 1];
			
			//relatively straight forward sort-merge-intersection
			if(ls != le && rs != re){
				int li = ls;
				int ri = rs;
				while(li < le && ri < re){
					final int l = csr[li];
					final int r = other.csr[ri];
					
					if(l == r){
						out.addTarget(l);
						li++;
						ri++;
					}else if(l < r){
						li++;
					}else{
						ri++;
					}
				}
			}
		}
		
		out.endFinalSource();
		return out;
	}
	
//	/**
//	 * Computes the join of the given left and right input graphs by
//	 * extending paths in the left input graph with paths in the right
//	 * input graph if the target vertex of a left input path is equal
//	 * to the source vertex of a right input path.
//	 * @param left The left input graph containing path prefixes.
//	 * @param right The right input graph containing path suffixes.
//	 * @return The result graph representing the join of the input graphs.
//	 */
	//assumed same vertex count
	public ResultGraph join(ResultGraph right){
		assert vertexCount == right.vertexCount;
		ResultGraph out = new ResultGraph(vertexCount, getEdgeCount() + right.getEdgeCount(), false);
		
		SmartBitSet seen = new SmartBitSet(out.vertexCount);
		for(int source = 0; source < vertexCount; source++){
			out.setActiveSource(source);
			
			final int ls = csr[source];
			final int le = csr[source + 1];
			if(ls != le){
				seen.rangeClear();

				for(int li = ls; li < le; li++){
					final int mid = csr[li];
					final int rs = right.csr[mid];
					for(int ri = rs; ri < right.csr[mid + 1]; ri++){
						final int target = right.csr[ri];
						if(!seen.get(target)){
							seen.rangeSet(target);
							out.addTarget(target);
						}
					}
				}
			}
		}
		
		out.endFinalSource();
		return out;
	}
	
	/**
	 * Computes the transitive closure of this graph. Note that the transitive closure
	 * is the smallest graph that contains the entire input graph and is also transitive.
	 * @return A new graph representing the transitive closure of this result graph.
	 */
	public ResultGraph transitiveClosure(){
		ResultGraph out = new ResultGraph(this, vertexCount * 2, false);
		
		Deque<Integer> stack = new ArrayDeque<Integer>(vertexCount);
		SmartBitSet seen = new SmartBitSet(vertexCount);
		
		for(int source = 0; source < vertexCount; source++){
			out.setActiveSource(source);
			
			if(csr[source] != csr[source + 1]){
				stack.push(source);
				seen.rangeClear();
				
				while(!stack.isEmpty()){
					int vertex = stack.pop();
					
					final int from = csr[vertex];
					final int to = csr[vertex + 1];
					for(int i = from; i < to; i++){
						int target = csr[i];
						if(!seen.get(target)){
							out.addTarget(target);
							seen.rangeSet(target);
							stack.push(target);
						}
					}
				}
			}
		}
		
		out.endFinalSource();
		return out;
	}
	
	public ResultGraph transitiveClosureFrom(int boundSource){
		ResultGraph out = new ResultGraph(this, vertexCount, false);
		
		Deque<Integer> stack = new ArrayDeque<Integer>(vertexCount);
		BitSet seen = new BitSet(vertexCount);
		
		for(int source = 0; source < vertexCount; source++){
			out.setActiveSource(source);
			
			if(source == boundSource && csr[source] != csr[source + 1]){
				stack.push(source);
				
				while(!stack.isEmpty()){
					int vertex = stack.pop();
					
					final int from = csr[vertex];
					final int to = csr[vertex + 1];
					for(int i = from; i < to; i++){
						int target = csr[i];
						if(!seen.get(target)){
							out.addTarget(target);
							seen.set(target);
							stack.push(target);
						}
					}
				}
			}
		}
		
		out.endFinalSource();
		return out;
	}
	
	public ResultGraph transitiveClosureTo(int boundTarget){
		ResultGraph out = new ResultGraph(this, vertexCount, false);
		
		Deque<Integer> stack = new ArrayDeque<Integer>(vertexCount);
		SmartBitSet seen = new SmartBitSet(vertexCount);
		
		sourceLoop: for(int source = 0; source < vertexCount; source++){
			out.setActiveSource(source);
			
			if(csr[source] != csr[source + 1]){
				stack.clear();
				stack.push(source);
				seen.rangeClear();
				
				while(!stack.isEmpty()){
					int vertex = stack.pop();
					
					final int from = csr[vertex];
					final int to = csr[vertex + 1];
					for(int i = from; i < to; i++){
						int target = csr[i];
						if(!seen.get(target)){
							if(target == boundTarget){
								out.addTarget(target);
								continue sourceLoop;
							}
							
							seen.rangeSet(target);
							stack.push(target);
						}
					}
				}
			}
		}
		
		out.endFinalSource();
		return out;
	}
	
	public ResultGraph transitiveClosure(int boundSource, int boundTarget){
		Deque<Integer> stack = new ArrayDeque<Integer>(vertexCount);
		BitSet seen = new BitSet(vertexCount);

		if(csr[boundSource] != csr[boundSource + 1]){
			stack.push(boundSource);

			while(!stack.isEmpty()){
				int vertex = stack.pop();

				final int from = csr[vertex];
				final int to = csr[vertex + 1];
				for(int i = from; i < to; i++){
					int target = csr[i];
					if(!seen.get(target)){
						if(target == boundTarget){
							return single(vertexCount, boundSource, boundTarget);
						}

						seen.set(target);
						stack.push(target);
					}
				}
			}
		}

		return empty(vertexCount);
	}

	public ResultGraph selectIdentity(){
		ResultGraph out = new ResultGraph(vertexCount, vertexCount, true);

		if(sorted){
			for(int source = 0; source < vertexCount; source++){
				out.setActiveSource(source);
				if(Arrays.binarySearch(csr, csr[source], csr[source + 1], source) >= 0){
					out.addTarget(source);
				}
			}
		}else{
			for(int source = 0; source < vertexCount; source++){
				out.setActiveSource(source);
				
				final int from = csr[source];
				final int to = csr[source + 1];
				for(int i = from; i < to; i++){
					if(csr[i] == source){
						out.addTarget(source);
						break;
					}
				}
			}
		}
		
		out.endFinalSource();
		return out;
	}
	
	/**
	 * Selects all the edges from this graph that start at the given source node.
	 * @param source The source node of the edges.
	 * @return A copy of this graph containing only the edges that started at the given source vertex.
	 */
	public ResultGraph selectSource(int source){
		ResultGraph out = new ResultGraph(vertexCount, vertexCount, sorted);
		
		for(int i = 0; i < vertexCount; i++){
			out.setActiveSource(i);
			
			if(i == source){
				final int from = csr[source];
				final int to = csr[source + 1];
				for(int idx = from; idx < to; idx++){
					out.addTarget(csr[idx]);
				}
			}
		}
		
		out.endFinalSource();
		return out;
	}
	
//	/**
//	 * Selects all the edges from the given input graph that end at the given target node.
//	 * @param target The target node of the edges.
//	 * @param in The input graph to select edges from.
//	 * @return A copy of the input graph containing only the edges that ended at the given target vertex.
//	 */
	public ResultGraph selectTarget(int target){
		ResultGraph out = new ResultGraph(vertexCount, vertexCount, true);
		
		for(int source = 0; source < vertexCount; source++){
			out.setActiveSource(source);
			
			final int from = csr[source];
			final int to = csr[source + 1];
			for(int i = from; i < to; i++){
				if(csr[i] == target){
					out.addTarget(target);
					break;
				}
			}
		}
		
		out.endFinalSource();
		return out;
	}
	
	/**
	 * Computes cardinality statistics for the result contained in this graph.
	 * @return Cardinality statistics for this result graph.
	 * @see CardStat
	 */
	public CardStat computeCardinality(){
		int out = 0;
		for(int source = 0; source < vertexCount; source++){
			if(csr[source] != csr[source + 1]){
				out++;
			}
		}
		
		BitSet in = new BitSet(vertexCount);
		for(int i = csr[0]; i < head; i++){
			in.set(csr[i]);
		}
		
		return new CardStat(out, getEdgeCount(), in.cardinality());
	}

	/**
	 * Gets the source target pairs in this result graph. This is the
	 * actual database operation or query evaluation result output.
	 * @return The paths matched by the database operation or query.
	 * @see SourceTargetPair
	 */
	public List<SourceTargetPair> getSourceTargetPairs(){
		List<SourceTargetPair> edges = new ArrayList<SourceTargetPair>();
		
		for(int source = 0; source < vertexCount; source++){
			final int from = csr[source];
			final int to = csr[source + 1];
			for(int i = from; i < to; i++){
				edges.add(new SourceTargetPair(source, csr[i]));
			}
		}
		
		return edges;
	}
	
	protected int[] getData(){
		return csr;
	}
	
	//NOTE: the below factory methods do not create a writable graph
	public static final ResultGraph empty(int vertexCount){
		return new ResultGraph(vertexCount);
	}
	
	public static final ResultGraph single(int vertexCount, int source, boolean sorted, int from, int to, int[] targets){
		return new ResultGraph(vertexCount, source, sorted, from, to, targets);
	}
	
	public static final ResultGraph single(int vertexCount, int source, boolean sorted, int... targets){
		return new ResultGraph(vertexCount, source, sorted, 0, targets.length, targets);
	}
	
	public static final ResultGraph single(int vertexCount, int source, int target){
		return new ResultGraph(vertexCount, source, true, 0, 1, target);
	}
}
