package dev.roanh.gmark.core.graph;

public class Type{
	private int id;//also called type
	private String alias;//textual type name
	private int size;//fixed number of nodes present of this type -- possibly not applicable if proportion is used
	protected boolean scalable;//TODO private
	private double proportion;//fraction of all nodes that have this type -- possibly not applicable if the node is present in a fixed quantity
	
	/**
	 * Checks if nodes of this type scale in number
	 * with the size of the graph. If this is false
	 * then nodes of this type are instead present
	 * in a fixed quantity.
	 * @return True if nodes of this type are scalable.
	 */
	public boolean isScalable(){
		return scalable;
	}
}
