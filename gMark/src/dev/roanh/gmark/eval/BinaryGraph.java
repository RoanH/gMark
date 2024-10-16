package dev.roanh.gmark.eval;

import java.util.Arrays;

import dev.roanh.gmark.util.SmartBitSet;

public class BinaryGraph{
	private final int vertexCount;
	private int[] csr;
	private int head;
	
	public BinaryGraph(int vertexCount, int sizeEstimate){
		this.vertexCount = vertexCount;
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
	public BinaryGraph union(BinaryGraph other){
		BinaryGraph out = new BinaryGraph(Math.max(vertexCount, other.vertexCount), getEdgeCount() + other.getEdgeCount());
		
		SmartBitSet seen = new SmartBitSet(out.vertexCount);
		for(int source = 0; source < out.vertexCount; source++){
			out.setActiveSource(source);
			seen.rangeClear();
			
			for(int i = csr[source]; i < csr[source + 1]; i++){
				int target = csr[i];
				out.addTarget(target);
				seen.rangeSet(target);
			}
			
			for(int i = other.csr[source]; i < other.csr[source + 1]; i++){
				int target = other.csr[i];
				if(!seen.get(target)){
					out.addTarget(target);
				}
			}
		}
		
		out.endFinalSource();
		return out;
	}
	
	public BinaryGraph intersection(BinaryGraph other){
		BinaryGraph out = new BinaryGraph(Math.max(vertexCount, other.vertexCount), Math.min(getEdgeCount(), other.getEdgeCount()));
		
		for(int source = 0; source < out.vertexCount; source++){
			out.setActiveSource(source);

			int ls = csr[source];
			int le = csr[source + 1];
			int rs = other.csr[source];
			int re = other.csr[source + 1];
			
			//relatively straight forward sort-merge-intersection
			if(ls != le && rs != re){
				Arrays.sort(csr, ls, le);
				Arrays.sort(other.csr, rs, le);
				
				int li = ls;
				int ri = rs;
				while(li < le && ri < re){
					int l = csr[li];
					int r = other.csr[ri];
					
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
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
