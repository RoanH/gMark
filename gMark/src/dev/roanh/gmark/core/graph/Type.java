package dev.roanh.gmark.core.graph;

import dev.roanh.gmark.util.IDable;

public class Type implements IDable{
	private int id;//also called type
	private String alias;//textual type name
	private int fixed;//fixed number of nodes present of this type -- possibly not applicable if proportion is used (set to -1)
	private boolean scalable;
	private double proportion;//fraction of all nodes that have this type -- possibly not applicable if the node is present in a fixed quantity (set to nan)
	
	public Type(int id, String alias, int fixed){
		this.id = id;
		this.alias = alias;
		this.fixed = fixed;
		proportion = Double.NaN;
		scalable = false;
	}
	
	public Type(int id, String alias, double proportion){
		this.id = id;
		this.alias = alias;
		this.proportion = proportion;
		fixed = -1;
		scalable = true;
	}
	
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
	
	public String getAlias(){
		return alias;
	}
	
	@Override
	public int getID(){
		return id;
	}
	
	@Override
	public String toString(){
		if(fixed == -1){
			return "Type[typeID=" + id + ",alias=\"" + alias + "\",proportion=" + proportion + "]";
		}else{
			return "Type[typeID=" + id + ",alias=\"" + alias + "\",fixed=" + fixed + "]";
		}
	}
}
