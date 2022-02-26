package dev.roanh.gmark.core.graph;

import java.util.Objects;

import dev.roanh.gmark.output.XML;
import dev.roanh.gmark.util.IndentWriter;

/**
 * Class describing predicates applied to graph
 * edges, also called symbols. These predicates
 * can also represent following an edge in its
 * inverse direction from target to source.
 * @author Roan
 */
public class Predicate implements XML{
	/**
	 * The unique ID of this predicate. This ID uniquely
	 * identifies this predicate among all predicates.
	 * Note that the inverse predicate has the same ID.
	 */
	private int id;
	private String alias;//textual symbol name
	private double proportion;//fraction of the graph edges that have this symbol - set to NaN for not specified
	private boolean isInverse = false;
	private Predicate inverse = null;
	
	//TODO prop is NaN for unknown, may want to change that
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
	 * True if this predicate represents the inverted
	 * form of the symbol (going from target to source).
	 * @return True if this is an inverse predicate.
	 */
	public boolean isInverse(){
		return isInverse;
	}
	
	/**
	 * Gets the textual representation
	 * for this symbol. If this predicate
	 * is inverted then this will include
	 * a super script minus character at the end.
	 * @return The predicate alias.
	 */
	public String getAlias(){
		return isInverse ? (alias + "\u207B") : alias;
	}
	
	/**
	 * Gets the inverse predicate for this predicate. The
	 * inverse predicate basically indicates traversing an
	 * edge with the predicate in the reverse direction
	 * from target to source and is indicated by a super
	 * script minus after the predicate symbol.
	 * @return The inverse of this predicate.
	 */
	public Predicate getInverse(){
		return inverse == null ? (inverse = new Predicate(this, !isInverse)) : inverse;
	}
	
	/**
	 * Gets the unique ID of this predicate. Uniquely identifies
	 * this predicate among all predicates. Note that the
	 * inverse predicate has the same ID.
	 * @return The unique ID of this predicate.
	 */
	public int getID(){
		return id;
	}
	
	//selects all pairs
	public String toSQL(){
		if(isInverse()){
			return "(SELECT trg AS src, src AS trg FROM edge WHERE label = " + id + ")";
		}else{
			return "(SELECT src, trg FROM edge WHERE label = " + id + ")";
		}
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

	@Override
	public void writeXML(IndentWriter writer){
		writer.print(isInverse() ? "<symbol inverse=\"true\">" : "<symbol>");
		writer.print(id);
		writer.println("</symbol>");
	}
}
