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

import dev.roanh.gmark.lang.QueryLanguage;
import dev.roanh.gmark.util.IndentWriter;

public class KleeneRPQ implements RPQ{
	private final RPQ rpq;
	
	public KleeneRPQ(RPQ rpq){
		this.rpq = rpq;
	}
	
	@Override
	public String toString(){
		return rpq.toString() + String.valueOf(QueryLanguage.CHAR_KLEENE);
	}

	@Override
	public void writeSQL(IndentWriter writer){
		// TODO Auto-generated method stub
		
	}

	@Override
	public void writeXML(IndentWriter writer){
		writer.println("<rpq type=\"kleene\">", 2);
		rpq.writeXML(writer);
		writer.println(2, "</rpq>");
	}
}
