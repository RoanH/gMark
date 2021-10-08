package dev.roanh.gmark.core.graph;

@Deprecated
public class Node{
	private int id;//also known as type or type id
	private Type type;
	
	/**
	 * Checks if nodes of this type scale in number
	 * with the size of the graph. If this is false
	 * then nodes of this type are instead present
	 * in a fixed quantity.
	 * @return True if nodes of this type are scalable.
	 */
	public boolean isScalable(){
		return type.scalable;
	}
}
