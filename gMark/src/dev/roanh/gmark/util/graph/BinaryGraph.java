package dev.roanh.gmark.util.graph;

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
	
	public void endSource(){
		csr[vertexCount] = head;
		//TODO possibly shrink array
	}
	
	
	
	
}
