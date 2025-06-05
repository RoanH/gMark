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
package dev.roanh.gmark.lang.cpq;

import dev.roanh.gmark.ast.Atom;
import dev.roanh.gmark.ast.OperationType;
import dev.roanh.gmark.ast.Variable;
import dev.roanh.gmark.lang.cpq.QueryGraphCPQ.Vertex;
import dev.roanh.gmark.lang.generic.GenericVariable;
import dev.roanh.gmark.util.IndentWriter;

/**
 * CPQ modelling the identity CPQ. Intended to be used
 * as a singleton via {@link CPQ#IDENTITY}.
 * @author Roan
 */
public final class IdentityCPQ implements CPQ, Atom{
	
	/**
	 * Prevent outside construction.
	 */
	protected IdentityCPQ(){
	}
	
	@Override
	public String toFormalSyntax(){
		return "id";
	}
	
	@Override
	public String toString(){
		return toFormalSyntax();
	}
	
	@Override
	public void writeSQL(IndentWriter writer){
		writer.println("SELECT src, trg FROM (", 2);
		writer.println("SELECT src, src AS trg FROM edge");
		writer.println("UNION");
		writer.println("SELECT trg AS src, trg FROM edge");
		writer.decreaseIndent(2);
		writer.print(")");
	}

	@Override
	public void writeXML(IndentWriter writer){
		writer.println("<cpq type=\"identity\"></cpq>");
	}

	@Override
	public QueryGraphCPQ toQueryGraph(Vertex source, Vertex target){
		return new QueryGraphCPQ(source, target);
	}

	@Override
	public int getDiameter(){
		return 0;
	}

	@Override
	public boolean isLoop(){
		return true;
	}

	@Override
	public OperationType getOperationType(){
		return OperationType.IDENTITY;
	}

	@Override
	public Variable getSource(){
		return GenericVariable.SRC;
	}

	@Override
	public Variable getTarget(){
		return GenericVariable.TRG;
	}
}
