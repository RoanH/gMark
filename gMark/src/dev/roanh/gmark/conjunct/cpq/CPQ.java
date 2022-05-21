package dev.roanh.gmark.conjunct.cpq;

import dev.roanh.gmark.conjunct.cpq.QueryGraphCPQ.Vertex;
import dev.roanh.gmark.output.OutputSQL;
import dev.roanh.gmark.output.OutputXML;
import dev.roanh.gmark.util.IndentWriter;

/**
 * Interface for conjunctive path queries (CPQs).
 * @author Roan
 */
public abstract interface CPQ extends OutputSQL, OutputXML{
	/**
	 * Constant for the special identity CPQ.
	 */
	public static final CPQ IDENTITY = new CPQ(){
		@Override
		public String toString(){
			return "id";
		}

		@Override
		public String toSQL(){
			throw new IllegalStateException("Identity to OutputSQL not supported (and never generated).");
		}

		@Override
		public void writeXML(IndentWriter writer){
			writer.println("<cpq type=\"identity\"></cpq>");
		}

		@Override
		public QueryGraphCPQ toQueryGraph(Vertex source, Vertex target){
			return new QueryGraphCPQ(source, target);
		}
	};
	
	public default QueryGraphCPQ toQueryGraph(){
		return toQueryGraph(Vertex.SOURCE, Vertex.TARGET);
	}
	
	public abstract QueryGraphCPQ toQueryGraph(Vertex source, Vertex target);
}
