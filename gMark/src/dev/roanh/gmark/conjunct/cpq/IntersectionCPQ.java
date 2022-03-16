package dev.roanh.gmark.conjunct.cpq;

import dev.roanh.gmark.util.IndentWriter;

/**
 * CPQ modelling the intersection between two CPQs
 * (also known as the conjunction operation).
 * @author Roan
 */
public class IntersectionCPQ implements CPQ{
	/**
	 * The first CPQ of the intersection.
	 */
	private CPQ first;
	/**
	 * The second CPQ of the intersection.
	 */
	private CPQ second;
	
	/**
	 * Constructs a new intersection CPQ with
	 * the given two CPQs to intersect.
	 * @param first The first CPQ of the intersection.
	 * @param second The second CPQ of the intersection.
	 */
	public IntersectionCPQ(CPQ first, CPQ second){
		this.first = first;
		this.second = second;
		if(first == CPQ.IDENTITY){
			this.first = this.second;
			this.second = CPQ.IDENTITY;
		}
	}
	
	@Override
	public String toString(){
		return "(" + first + " ∩ " + second + ")";
	}

	@Override
	public String toSQL(){
		if(second == CPQ.IDENTITY){
			return "(SELECT ii.src AS src, ii.trg AS trg FROM " + first.toSQL() + " AS ii WHERE ii.src = ii.trg)";
		}else{
			return "(" + first.toSQL() + " INTERSECT " + second.toSQL() + ")";
		}
	}

	@Override
	public void writeXML(IndentWriter writer){
		writer.println("<cpq type=\"intersect\">", 2);
		first.writeXML(writer);
		second.writeXML(writer);
		writer.println(2, "</cpq>");
	}
}
