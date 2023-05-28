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

import java.util.List;
import java.util.StringJoiner;

import dev.roanh.gmark.conjunct.cpq.QueryGraphCPQ.Vertex;
import dev.roanh.gmark.util.IndentWriter;

/**
 * CPQ modelling the concatenation of a number of CPQs
 * (also known as the join operation).
 * @author Roan
 */
public class ConcatCPQ implements CPQ{
	/**
	 * In order the concatenated CPQs.
	 */
	private List<CPQ> cpq;
	
	/**
	 * Constructs a new concat CPQ with the
	 * given list of CPQs to concatenate.
	 * @param cpq The CPQs to concatenate.
	 * @throws IllegalArgumentException When the
	 *         list of CPQs is empty.
	 */
	public ConcatCPQ(List<CPQ> cpq){
		this.cpq = cpq;
		if(cpq.isEmpty()){
			throw new IllegalArgumentException("List of CPQs to concatenate cannot be empty.");
		}
	}
	
	@Override
	public String toString(){
		StringJoiner builder = new StringJoiner(String.valueOf(CPQ.CHAR_JOIN), "(", ")");
		for(CPQ item : cpq){
			builder.add(item.toString());
		}
		return builder.toString();
	}

	@Override
	public String toSQL(){
		if(cpq.size() == 1){
			return cpq.get(0).toSQL();
		}
		
		StringBuilder buffer = new StringBuilder();
		int n = cpq.size();
		
		buffer.append("(SELECT s0.src AS src, s");
		buffer.append(n - 1);
		buffer.append(".trg AS trg FROM ");
		for(int i = 0; i < n; i++){
			buffer.append(cpq.get(i).toSQL());
			buffer.append(" AS s");
			buffer.append(i);
			if(i < n - 1){
				buffer.append(", ");
			}
		}
		
		buffer.append(" WHERE ");
		for(int i = 0; i < n - 1; i++){
			buffer.append("s");
			buffer.append(i);
			buffer.append(".trg = s");
			buffer.append(i + 1);
			buffer.append(".src");
			if(i < n - 2){
				buffer.append(" AND ");
			}
		}
		
		buffer.append(")");
		return buffer.toString();
	}

	@Override
	public void writeXML(IndentWriter writer){
		writer.println("<cpq type=\"concat\">", 2);
		cpq.forEach(c->c.writeXML(writer));
		writer.println(2, "</cpq>");
	}

	@Override
	public QueryGraphCPQ toQueryGraph(Vertex source, Vertex target){
		if(cpq.size() == 1){
			return cpq.get(0).toQueryGraph(source, target);
		}
		
		Vertex mid = new Vertex();
		QueryGraphCPQ chain = cpq.get(0).toQueryGraph(source, mid);
		for(int i = 1; i < cpq.size(); i++){
			Vertex to = i == cpq.size() - 1 ? target : new Vertex();
			chain.union(cpq.get(i).toQueryGraph(mid, to));
			mid = to;
		}
		
		chain.setTarget(target);
		return chain;
	}

	@Override
	public int getDiameter(){
		return cpq.stream().mapToInt(CPQ::getDiameter).sum();
	}

	@Override
	public boolean isLoop(){
		return false;
	}
}
