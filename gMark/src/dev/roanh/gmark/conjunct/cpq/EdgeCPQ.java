package dev.roanh.gmark.conjunct.cpq;

import dev.roanh.gmark.conjunct.cpq.QueryGraphCPQ.Vertex;
import dev.roanh.gmark.core.graph.Predicate;
import dev.roanh.gmark.util.IndentWriter;

/**
 * CPQ modelling a single label traversal.
 * @author Roan
 */
public class EdgeCPQ implements CPQ{
	/**
	 * The label traversed by this CPQ.
	 */
	private Predicate symbol;
	
	/**
	 * Constructs a new edge CPQ with the
	 * given label to traverse.
	 * @param symbol The label to traverse.
	 */
	public EdgeCPQ(Predicate symbol){
		this.symbol = symbol;
	}
	
	public Predicate getLabel(){
		return symbol;
	}
	
	@Override
	public String toString(){
		return symbol.getAlias();
	}

	@Override
	public String toSQL(){
		return symbol.toSQL();
	}

	@Override
	public void writeXML(IndentWriter writer){
		symbol.writeXML(writer);
	}

	@Override
	public QueryGraphCPQ toQueryGraph(Vertex source, Vertex target){
		return new QueryGraphCPQ(this, source, target);
	}
}
