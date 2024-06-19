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
 * CPQ modelling the identity CPQ. Intended to be used
 * as a singleton via {@link CPQ#IDENTITY}.
 * @author Roan
 */
public final class IdentityCPQ implements CPQ{
	
	/**
	 * Prevent outside construction.
	 */
	protected IdentityCPQ(){
	}
	
	@Override
	public String toString(){
		return "id";
	}
	
	@Override
	public void writeSQL(IndentWriter writer){
		writer.print("(SELECT src, src AS trg FROM edge) UNION (SELECT trg AS src, trg FROM edge)");
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
}
