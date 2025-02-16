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
package dev.roanh.gmark.util.graph.specific;

import java.util.Map;

import dev.roanh.gmark.data.SelectivityType;
import dev.roanh.gmark.type.SelectivityClass;
import dev.roanh.gmark.type.schema.Edge;
import dev.roanh.gmark.type.schema.Predicate;
import dev.roanh.gmark.type.schema.Schema;
import dev.roanh.gmark.type.schema.Type;
import dev.roanh.gmark.util.RangeList;
import dev.roanh.gmark.util.Util;
import dev.roanh.gmark.util.graph.generic.UniqueGraph;

/**
 * The schema graph is a graph that has selectivity types
 * as its nodes. These nodes represent the combination of
 * some node type and some selectivity class. There are
 * directed labelled edges present between these selectivity
 * type nodes. These edges indicate that it is possible to
 * extend a path query with an edge with a certain label to
 * end up at the target selectivity type.
 * @author Roan
 * @see SelectivityType
 * @see Predicate
 */
public class SchemaGraph extends UniqueGraph<SelectivityType, Predicate>{
	/**
	 * Efficient lookup index from selectivity type to graph node.
	 */
	private RangeList<Map<SelectivityClass, GraphNode<SelectivityType, Predicate>>> index;

	/**
	 * Constructs a new schema graph from the given schema.
	 * @param schema The graph schema to build the schema
	 *        graph from.
	 */
	public SchemaGraph(Schema schema){
		index = new RangeList<Map<SelectivityClass, GraphNode<SelectivityType, Predicate>>>(schema.getTypeCount(), Util.selectivityMapSupplier());
		
		for(Edge edge : schema.getEdges()){
			SelectivityClass sel2 = edge.getSelectivity();
			for(SelectivityClass sel1 : SelectivityClass.values()){
				resolve(edge.getSourceType(), sel1).addUniqueEdgeTo(resolve(edge.getTargetType(), sel1.conjunction(sel2)), edge.getPredicate());
				resolve(edge.getTargetType(), sel1).addUniqueEdgeTo(resolve(edge.getSourceType(), sel1.conjunction(sel2.negate())), edge.getPredicate().getInverse());
			}
		}
	}
	
	/**
	 * Resolves the given selectivity type presented as
	 * a type and selectivity class to the associated
	 * graph node adding a new node if required.
	 * @param type The type of the selectivity type.
	 * @param sel The selectivity of the selectivity type.
	 * @return The graph node associated with the given selectivity type.
	 */
	private GraphNode<SelectivityType, Predicate> resolve(Type type, SelectivityClass sel){
		return index.get(type).computeIfAbsent(sel, k->addUniqueNode(new SelectivityType(type, sel)));
	}
	
	/**
	 * Removes all nodes (and associated edges) from the schema graph that are not
	 * reachable when drawing paths. All valid paths should start at a node with a
	 * selectivity class of {@link SelectivityClass#ONE_ONE} or {@link SelectivityClass#EQUALS}.
	 * Any node that is not reachable from a node with one of these selectivity classes
	 * is removed together with all the edges connected to it.
	 */
	public void removeUnreachable(){
		int removed;
		do{
			removed = removeNodeIf(node->{
				SelectivityClass sel = node.getData().getSelectivity();
				if(sel == SelectivityClass.ONE_ONE || sel == SelectivityClass.EQUALS){
					return false;
				}else{
					if(node.getInEdges().isEmpty()){
						return true;
					}else{
						//ensure we remove nodes that only have a self loop
						for(GraphEdge<SelectivityType, Predicate> edge : node.getInEdges()){
							if(!edge.getSourceNode().equals(node)){
								return false;
							}
						}
						return true;
					}
				}
			}).size();
		}while(removed != 0);
	}
	
	/**
	 * Prints all the nodes in the schema graph
	 * to the standard output.
	 */
	public void printNodes(){
		getNodes().forEach(System.out::println);
	}
	
	/**
	 * Prints all the edges in the schema graph
	 * to the standard output.
	 */
	public void printEdges(){
		for(GraphEdge<SelectivityType, Predicate> edge : getEdges()){
			System.out.println(edge.getSource() + " -> " + edge.getData().getAlias() + " -> " + edge.getTarget());
		}
	}
}
