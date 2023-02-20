/*
 * gMark: A domain- and query language-independent graph instance and query workload generator.
 * Copyright (C) 2021  Roan Hofland (roan@roanh.dev).  All rights reserved.
 * GitHub Repository: https://github.com/RoanH/gMark
 *
 * gMark is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * gMark is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package dev.roanh.gmark.conjunct.cpq;

import dev.roanh.gmark.conjunct.cpq.QueryGraphCPQ.Vertex;
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
		return "(" + first + " " + CPQ.CHAR_CAP + " " + second + ")";
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

	@Override
	public QueryGraphCPQ toQueryGraph(Vertex source, Vertex target){
		return first.toQueryGraph(source, target).union(second.toQueryGraph(source, target));
	}
}
