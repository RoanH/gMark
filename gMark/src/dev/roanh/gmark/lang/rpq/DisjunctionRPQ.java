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
package dev.roanh.gmark.lang.rpq;

import java.util.List;
import java.util.StringJoiner;

import dev.roanh.gmark.ast.OperationType;
import dev.roanh.gmark.ast.QueryTree;
import dev.roanh.gmark.lang.QueryLanguageSyntax;
import dev.roanh.gmark.util.IndentWriter;

/**
 * RPQ modelling the disjunction between RPQs.
 * @author Roan
 */
public class DisjunctionRPQ implements RPQ{
	/**
	 * The RPQs of the disjunction.
	 */
	private final List<RPQ> rpq;
	
	/**
	 * Constructs a new disjunction RPQ with the
	 * given RPQs as the components.
	 * @param rpq The RPQs for the disjunction.
	 * @throws IllegalArgumentException When less than two RPQs are provided.
	 */
	public DisjunctionRPQ(List<RPQ> rpq) throws IllegalArgumentException{
		this.rpq = rpq;
		if(rpq.size() < 2){
			throw new IllegalArgumentException("Not enough RPQs given (need at least 2)");
		}
	}

	@Override
	public String toString(){
		StringJoiner builder = new StringJoiner(" " + QueryLanguageSyntax.CHAR_DISJUNCTION + " ", "(", ")");
		
		for(RPQ item : rpq){
			builder.add(item.toString());
		}
		
		return builder.toString();
	}
	
	@Override
	public void writeSQL(IndentWriter writer){
		writer.println("SELECT src, trg FROM (", 2);
		for(int i = 0; i < rpq.size(); i++){
			writer.println("SELECT src, trg FROM (", 2);
			rpq.get(i).writeSQL(writer);
			writer.println();
			writer.println(2, ")");
			if(i < rpq.size() - 1){
				writer.println("UNION");
			}
		}
		writer.decreaseIndent(2);
		writer.print(")");
	}

	@Override
	public void writeXML(IndentWriter writer){
		writer.println("<rpq type=\"disj\">", 2);
		rpq.forEach(c->c.writeXML(writer));
		writer.println(2, "</rpq>");
	}

	@Override
	public OperationType getOperationType(){
		return OperationType.DISJUNCTION;
	}

	@Override
	public QueryTree toAbstractSyntaxTree(){
		QueryTree right = rpq.get(rpq.size() - 1).toAbstractSyntaxTree();
		for(int i = rpq.size() - 2; i >= 0; i--){
			right = QueryTree.ofBinary(rpq.get(i).toAbstractSyntaxTree(), right, this);
		}
		
		return right;
	}
}
