package dev.roanh.gmark.core.graph;

public class Predicate{
	private int id;//also called symbol
	private String alias;//textual symbol name
	private double proportion;//fraction of the graph edges that have this symbol
	
	public Predicate(int id, String alias, double proportion){
		this.id = id;
		this.alias = alias;
		this.proportion = proportion;
	}
	
	public int getID(){
		return id;
	}
	
	@Override
	public String toString(){
		return "Predicate[symbolID=" + id + ",alias=\"" + alias + "\",proportion=" + proportion + "]";
	}
}
