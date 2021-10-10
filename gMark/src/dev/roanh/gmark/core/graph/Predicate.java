package dev.roanh.gmark.core.graph;

public class Predicate{
	private int id;//also called symbol
	private String alias;//textual symbol name
	private double proportion;//fraction of the graph edges that have this symbol
	private boolean isInverse = false;
	private Predicate inverse = null;
	
	public Predicate(int id, String alias, double proportion){
		this.id = id;
		this.alias = alias;
		this.proportion = proportion;
	}
	
	private Predicate(Predicate predicate, boolean inverse){
		this(predicate.id, predicate.alias, predicate.proportion);
		isInverse = inverse;
		this.inverse = predicate;
	}
	
	public int getID(){
		return id;
	}
	
	public Predicate getInverse(){
		return inverse == null ? new Predicate(this, !isInverse) : inverse;
	}
	
	@Override
	public String toString(){
		return "Predicate[symbolID=" + id + ",alias=\"" + alias + "\",proportion=" + proportion + "]";
	}
}
