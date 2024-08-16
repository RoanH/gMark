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

import dev.roanh.gmark.lang.cpq.QueryGraphCPQ.Vertex;
import dev.roanh.gmark.lang.generic.GenericConcatenation;
import dev.roanh.gmark.util.IndentWriter;

/**
 * CPQ modelling the concatenation of a number of CPQs
 * (also known as the join operation).
 * @author Roan
 */
public class ConcatCPQ extends GenericConcatenation<CPQ> implements CPQ{
	
	/**
	 * Constructs a new concat CPQ with the
	 * given list of CPQs to concatenate.
	 * @param cpqs The CPQs to concatenate.
	 * @throws IllegalArgumentException When the
	 *         list of CPQs is empty.
	 */
	public ConcatCPQ(List<CPQ> cpqs) throws IllegalArgumentException{
		super(cpqs);
	}
	
	@Override
	public void writeXML(IndentWriter writer){
		writer.println("<cpq type=\"concat\">", 2);
		elements.forEach(c->c.writeXML(writer));
		writer.println(2, "</cpq>");
	}

	@Override
	public QueryGraphCPQ toQueryGraph(Vertex source, Vertex target){
		if(elements.size() == 1){
			return elements.get(0).toQueryGraph(source, target);
		}
		
		Vertex mid = new Vertex();
		QueryGraphCPQ chain = elements.get(0).toQueryGraph(source, mid);
		for(int i = 1; i < elements.size(); i++){
			Vertex to = i == elements.size() - 1 ? target : new Vertex();
			chain.union(elements.get(i).toQueryGraph(mid, to));
			mid = to;
		}
		
		chain.setTarget(target);
		return chain;
	}

	@Override
	public int getDiameter(){
		return elements.stream().mapToInt(CPQ::getDiameter).sum();
	}

	@Override
	public boolean isLoop(){
		return false;
	}
}
