package dev.roanh.gmark.eval;

import dev.roanh.gmark.core.graph.Predicate;

public class DatabaseGraph{
	private final int vertexCount;
	private final int[] syn1;
	protected final int[] slt;
	protected final int[] reverseSlt;
	
	
	
	
	
	
	
	public int getVertexCount(){
		return vertexCount;
	}
	
//	public int getEdgeCount(){
//		
//	}
	
	public int getEdgeCount(Predicate label){
		return syn1[label.getID()];
	}
	
	public int getLabelCount(){
		return syn1.length;
	}
	
	public ResultGraph selectLabel(Predicate label){
		ResultGraph out = new ResultGraph(vertexCount, getEdgeCount(label));
		
		final int[] data = label.isInverse() ? reverseSlt : slt;
		for(int source = 0; source < vertexCount; source++){
			out.setActiveSource(source);
			
			final int start = data[source];
			if(start != data[source + 1]){
				final int labelStart = data[start + label.getID()];
				final int labelEnd = data[start + label.getID() + 1];
				for(int i = labelStart; i < labelEnd; i++){
					out.addTarget(data[i]);
				}
			}
		}
		
		out.endFinalSource();
		return out;
	}
	
//	/**
//	 * Selects all the vertices from the given input database graph. Note that vertices
//	 * are selected together with themselves to form a complete source target pair.
//	 * @return A copy of the input graph containing only vertices selected with themselves.
//	 */
	public ResultGraph selectIdentity(){
		ResultGraph out = new ResultGraph(vertexCount, vertexCount);

		for(int vertex = 0; vertex < vertexCount; vertex++){
			out.setActiveSource(vertex);
			out.addTarget(vertex);
		}
		
		out.endFinalSource();
		return out;
	}
}
