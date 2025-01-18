/*
 * gMark: A domain- and query language-independent query workload generator and query language utility library.
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

import dev.roanh.gmark.ast.OperationType;
import dev.roanh.gmark.ast.QueryTree;
import dev.roanh.gmark.lang.QueryLanguageSyntax;
import dev.roanh.gmark.util.IndentWriter;

/**
 * RPQ representing the transitive closure of another RPQ.
 * @author Roan
 */
public class KleeneRPQ implements RPQ{
	/**
	 * The RPQ under transitive closure.
	 */
	private final RPQ rpq;
	
	/**
	 * Constructs a new RPQ representing the transitive closure of the given RPQ.
	 * @param rpq The RPQ to take the transitive closure of.
	 */
	public KleeneRPQ(RPQ rpq){
		this.rpq = rpq;
	}
	
	@Override
	public String toFormalSyntax(){
		return rpq.toString() + String.valueOf(QueryLanguageSyntax.CHAR_KLEENE);
	}
	
	@Override
	public String toString(){
		return toFormalSyntax();
	}

	@Override
	public void writeSQL(IndentWriter writer){
		writer.println("WITH RECURSIVE");
		writer.println("base(src, trg) AS (", 2);
		rpq.writeSQL(writer);
		writer.println();
		writer.println(2, "),");
		writer.println("tc(src, trg) AS (", 2);
		writer.println("SELECT src, trg");
		writer.println("FROM base");
		writer.println("UNION");
		writer.println("SELECT head.src, tail.trg");
		writer.println("FROM base AS head, tc AS tail");
		writer.println("WHERE head.trg = tail.src");
		writer.println(2, ")");
		writer.print("SELECT base.src, base.trg FROM base, tc");
	}
	
	@Override
	public void writeXML(IndentWriter writer){
		writer.println("<rpq type=\"kleene\">", 2);
		rpq.writeXML(writer);
		writer.println(2, "</rpq>");
	}

	@Override
	public OperationType getOperationType(){
		return OperationType.KLEENE;
	}

	@Override
	public QueryTree toAbstractSyntaxTree(){
		return QueryTree.ofUnary(rpq.toAbstractSyntaxTree(), this);
	}
}
