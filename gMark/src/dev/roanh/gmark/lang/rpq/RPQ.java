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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import dev.roanh.gmark.core.graph.Predicate;
import dev.roanh.gmark.lang.QueryLanguage;
import dev.roanh.gmark.lang.cpq.CPQ;
import dev.roanh.gmark.lang.cpq.ConcatCPQ;
import dev.roanh.gmark.lang.cpq.EdgeCPQ;

/**
 * Interface for regular path queries (RPQs).
 * @author Roan
 */
public abstract interface RPQ extends QueryLanguage{//TODO outputs?
	

	
	
	
	
	public static RPQ kleene(RPQ rpq){
		
	}
	
	public static RPQ concat(RPQ... rpqs){
		
	}
	
	public static RPQ concat(List<RPQ> rpqs){
		
	}
	
	public static RPQ disjunction(RPQ first, RPQ second){//TODO vararg?
		
	}
	
	
	
	
	
	
	/**
	 * Returns an RPQ representing a single labelled edge traversal.
	 * @param label The label of the traversed edge.
	 * @return The label traversal RPQ.
	 */
	public static RPQ label(Predicate label){
		return new EdgeRPQ(label);
	}
	
	/**
	 * Returns an RPQ representing a chain of labelled edge traversals.
	 * @param labels The labels of the traversed edges.
	 * @return The label traversal RPQ.
	 */
	public static CPQ labels(Predicate... labels){
		return new ConcatRPQ(Arrays.stream(labels).map(EdgeRPQ::new).collect(Collectors.toList()));
	}
	
	/**
	 * Returns an RPQ representing a chain of labelled edge traversals.
	 * @param labels The labels of the traversed edges.
	 * @return The label traversal RPQ.
	 */
	public static CPQ labels(List<Predicate> labels){
		return new ConcatRPQ(labels.stream().map(EdgeRPQ::new).collect(Collectors.toList()));
	}
	
	
	
	
	
}
