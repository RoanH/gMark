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
package dev.roanh.gmark.lang.cpq;

import java.util.List;
import java.util.StringJoiner;

import dev.roanh.gmark.ast.OperationType;
import dev.roanh.gmark.ast.QueryTree;
import dev.roanh.gmark.lang.QueryLanguageSyntax;
import dev.roanh.gmark.lang.cpq.QueryGraphCPQ.Vertex;
import dev.roanh.gmark.util.IndentWriter;

/**
 * CPQ modelling the intersection between CPQs
 * (also known as the conjunction operation).
 * @author Roan
 */
public class IntersectionCPQ implements CPQ{
	/**
	 * The CPQs of the intersection.
	 */
	private final List<CPQ> cpq;
	
	/**
	 * Constructs a new intersection CPQ with
	 * the given CPQs to intersect.
	 * @param cpq The CPQs to intersect.
	 * @throws IllegalArgumentException When less than two CPQs are provided.
	 */
	public IntersectionCPQ(List<CPQ> cpq) throws IllegalArgumentException{
		this.cpq = cpq;
		if(cpq.size() < 2){
			throw new IllegalArgumentException("Not enough CPQs given (need at least 2)");
		}
	}
	
	@Override
	public void writeSQL(IndentWriter writer){
		writer.println("SELECT src, trg FROM (", 2);
		for(int i = 0; i < cpq.size(); i++){
			writer.println("SELECT src, trg FROM (", 2);
			cpq.get(i).writeSQL(writer);
			writer.println();
			writer.println(2, ")");
			if(i < cpq.size() - 1){
				writer.println("INTERSECT");
			}
		}
		writer.decreaseIndent(2);
		writer.print(")");
	}
	
	@Override
	public String toString(){
		StringJoiner builder = new StringJoiner(" " + QueryLanguageSyntax.CHAR_CAP + " ", "(", ")");
		
		for(CPQ item : cpq){
			builder.add(item.toString());
		}
		
		return builder.toString();
	}
	
	@Override
	public void writeXML(IndentWriter writer){
		writer.println("<cpq type=\"intersect\">", 2);
		cpq.forEach(c->c.writeXML(writer));
		writer.println(2, "</cpq>");
	}

	@Override
	public QueryGraphCPQ toQueryGraph(Vertex source, Vertex target){
		QueryGraphCPQ g = cpq.get(0).toQueryGraph(source, target);
		for(int i = 1; i < cpq.size(); i++){
			g.union(cpq.get(i).toQueryGraph(source, target));
		}
		return g;
	}

	@Override
	public int getDiameter(){
		int max = 0;
		for(CPQ q : cpq){
			max = Math.max(max, q.getDiameter());
		}
		return max;
	}

	@Override
	public boolean isLoop(){
		for(CPQ q : cpq){
			if(q.isLoop()){
				return true;
			}
		}
		return false;
	}

	@Override
	public OperationType getOperationType(){
		return OperationType.INTERSECTION;
	}

	@Override
	public QueryTree toAbstractSyntaxTree(){
		QueryTree right = cpq.get(cpq.size() - 1).toAbstractSyntaxTree();
		for(int i = cpq.size() - 2; i >= 0; i--){
			right = QueryTree.ofBinary(cpq.get(i).toAbstractSyntaxTree(), right, this);
		}
		
		return right;
	}
}
