package dev.roanh.gmark.core.graph;

import java.util.Objects;

import dev.roanh.gmark.output.OutputSQL;
import dev.roanh.gmark.output.OutputXML;
import dev.roanh.gmark.util.IndentWriter;

/**
 * Class describing predicates applied to graph
 * edges, also called symbols. These predicates
 * can also represent following an edge in its
 * inverse direction from target to source.
 * @author Roan
 */
public class Predicate implements OutputXML, OutputSQL{
	/**
	 * The unique ID of this predicate. This ID uniquely
	 * identifies this predicate among all predicates.
	 * Note that the inverse predicate has the same ID.
	 */
	private int id;
	/**
	 * The textual alias name of this predicate.
	 */
	private String alias;
	/**
	 * Fraction of all edges in the graph that
	 * have this symbol. Will be <code>null</code>
	 * if this is not specified.
	 */
	private Double proportion;
	/**
	 * True if this predicate represents the inverse
	 * of the original predicate, meaning it specifies
	 * that a directed edge should be followed in the
	 * reverse direction.
	 */
	private boolean isInverse = false;
	/**
	 * The inverse predicate object or <code>null</code>.
	 */
	private Predicate inverse = null;
	
	/**
	 * Constructs a new predicate with the given ID
	 * and alias and graph proportion.
	 * @param id The ID of this predicate.
	 * @param alias The alias of this predicate.
	 * @param proportion The fraction of all edges
	 *        in the graph that have this symbol.
	 *        Can be set to <code>null</code> to leave
	 *        this unspecified.
	 */
	public Predicate(int id, String alias, Double proportion){
		this.id = id;
		this.alias = alias;
		this.proportion = proportion;
	}
	
	/**
	 * Constructs an inverse predicate for the given predicate.
	 * @param predicate The predicate to invert.
	 */
	private Predicate(Predicate predicate){
		this(predicate.id, predicate.alias, predicate.proportion);
		isInverse = !predicate.isInverse;
		inverse = predicate;
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
		return inverse == null ? (inverse = new Predicate(this)) : inverse;
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
	
	@Override
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
