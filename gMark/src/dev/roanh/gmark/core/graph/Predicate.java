package dev.roanh.gmark.core.graph;

import java.util.Objects;

/**
 * Class describing predicates applied to graph
 * edges, also called symbols. These predicates
 * can also represent following an edge in its
 * inverse direction from target to source.
 * @author Roan
 */
public class Predicate{
	private int id;//also called symbol
	private String alias;//textual symbol name
	private double proportion;//fraction of the graph edges that have this symbol - set to NaN for not specified
	private boolean isInverse = false;
	private Predicate inverse = null;
	
	//prop is NaN for unknown, may want to change that
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
	
	/**
	 * Gets the textual representation
	 * for this symbol.
	 * @return The predicate alias.
	 */
	public String getAlias(){
		return isInverse ? (alias + "\u207B") : alias;
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
	
	@Override
	public boolean equals(Object other){
		if(other instanceof Predicate){
			Predicate p = (Predicate)other;
			return p.id == id && p.isInverse == isInverse;
		}else{
			return false;
		}
	}
	
	@Override
	public int hashCode(){
		return Objects.hash(id, isInverse);
	}
}
