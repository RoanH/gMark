package dev.roanh.gmark.eval;

import java.util.ArrayList;
import java.util.Arrays;
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
 * @author Roan
 * @see CardStat
 * @see SourceTargetPair
 */
public class ResultGraph{
	private final int vertexCount;
	private final boolean sorted;
	private int[] csr;
	private int head;
	
	protected ResultGraph(int vertexCount, int sizeEstimate, boolean sorted){
		this.vertexCount = vertexCount;
		this.sorted = sorted;
		csr = new int[vertexCount + 1 + sizeEstimate];
		head = vertexCount + 1;
	}
	
	public int getEdgeCount(){
		return csr[vertexCount] - csr[0];
	}
	
	public int getVertexCount(){
		return vertexCount;
	}
	
	public void setActiveSource(int source){
		csr[source] = head;
	}
	
	public void addTarget(int target){
		//TODO resize if required
		
		csr[head++] = target;
	}
	
	public void endFinalSource(){
		csr[vertexCount] = head;
		//TODO possibly shrink array
		
		
	}
	
//	/**
//	 * Computes the disjunction (or union) of the given left and right input graphs.
//	 * Recall that this operation simply added all the paths in both input graphs
//	 * to the result graph. Note that the order of the input arguments is irrelevant.
//	 * @param left The left input graph.
//	 * @param right The right input graph.
//	 * @return The result graph representing the union of the input graphs.
//	 */
	//assumed same vertex count
	public ResultGraph union(ResultGraph other){
		assert vertexCount == other.vertexCount;
		ResultGraph out = new ResultGraph(vertexCount, getEdgeCount() + other.getEdgeCount());
		
		SmartBitSet seen = new SmartBitSet(out.vertexCount);
		for(int source = 0; source < out.vertexCount; source++){
			out.setActiveSource(source);
			seen.rangeClear();
			
			final int ls = csr[source];
			final int le = csr[source + 1];
			for(int i = ls; i < le; i++){
				int target = csr[i];
				out.addTarget(target);
				seen.rangeSet(target);
			}
			
			final int rs = other.csr[source];
			final int re = other.csr[source + 1];
			for(int i = rs; i < re; i++){
				final int target = other.csr[i];
				if(!seen.get(target)){
					out.addTarget(target);
				}
			}
		}
		
		out.endFinalSource();
		return out;
	}
	
//	/**
//	 * Computes the intersection of the given left and right input graphs.
//	 * Recall that this operation simply discards all paths that are not
//	 * present in both input graphs. Note that the order of the input
//	 * arguments is irrelevant.
//	 * @param left The left input graph.
//	 * @param right The right input graph.
//	 * @return The result graph representing the intersection of the input graphs.
//	 */
	//assumed same vertex count
	public ResultGraph intersection(ResultGraph other){
		assert vertexCount == other.vertexCount;
		ResultGraph out = new ResultGraph(vertexCount, Math.min(getEdgeCount(), other.getEdgeCount()));
		
		for(int source = 0; source < out.vertexCount; source++){
			out.setActiveSource(source);

			final int ls = csr[source];
			final int le = csr[source + 1];
			final int rs = other.csr[source];
			final int re = other.csr[source + 1];
			
			//relatively straight forward sort-merge-intersection
			if(ls != le && rs != re){
				Arrays.sort(csr, ls, le);
				Arrays.sort(other.csr, rs, le);
				
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
		ResultGraph out = new ResultGraph(vertexCount, getEdgeCount() + right.getEdgeCount());
		
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
	
//	/**
//	 * Computes the transitive closure of the given input graph. Note that the transitive
//	 * closure is the smallest graph that contains the entire input graph and is also transitive.
//	 * @param in The input graph to compute the transitive closure of.
//	 * @return A new graph representing the transitive closure of the input graph.
//	 */
	public ResultGraph transitiveClosure(){
		 ResultGraph out = this;
		 
		 int lastSize = out.getEdgeCount();
		 while(true){
			 out = out.union(out.join(this));
			 if(out.getEdgeCount() != lastSize){
				 lastSize = out.getEdgeCount();
			 }else{
				 break;
			 }
		 }
		 
		 return out;
	}
	
//	/**
//	 * Selects all the edges from the given input graph that start at the given source node.
//	 * @param source The source node of the edges.
//	 * @param in The input graph to select edges from.
//	 * @return A copy of the input graph containing only the edges that started at the given source vertex.
//	 */
	//TODO this should not be in post
	public ResultGraph selectSource(int source){
		ResultGraph out = new ResultGraph(vertexCount, vertexCount);
		
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
	//TODO this should not be in post
	public ResultGraph selectTarget(int target){
		ResultGraph out = new ResultGraph(vertexCount, vertexCount);
		
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
	
	
	
	
	
	
	
	
	
}
