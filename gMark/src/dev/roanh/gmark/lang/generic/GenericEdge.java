package dev.roanh.gmark.lang.generic;

import dev.roanh.gmark.core.graph.Predicate;
import dev.roanh.gmark.output.OutputSQL;
import dev.roanh.gmark.output.OutputXML;
import dev.roanh.gmark.util.IndentWriter;

/**
 * Simple query action of traversing an edge with
 * a single label from either source to target or
 * in inverse direction from target to source.
 * @author Roan
 */
public class GenericEdge implements OutputSQL, OutputXML{
	/**
	 * The label traversed by this query.
	 */
	protected final Predicate symbol;

	/**
	 * Constructs a new generic edge with the given label to traverse.
	 * @param symbol The label to traverse.
	 */
	protected GenericEdge(Predicate symbol){
		this.symbol = symbol;
	}
	
	/**
	 * Gets the label (symbol) for this edge.
	 * @return The label for this edge.
	 */
	public Predicate getLabel(){
		return symbol;
	}
	
	@Override
	public String toString(){
		return symbol.getAlias();
	}
	
	@Override
	public void writeSQL(IndentWriter writer){
		if(symbol.isInverse()){
			writer.print("SELECT trg AS src, src AS trg FROM edge WHERE label = " + symbol.getID());
		}else{
			writer.print("SELECT src, trg FROM edge WHERE label = " + symbol.getID());
		}
	}
	
	@Override
	public void writeXML(IndentWriter writer){
		symbol.writeXML(writer);
	}
}
