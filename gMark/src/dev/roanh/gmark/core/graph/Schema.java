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
package dev.roanh.gmark.core.graph;

import java.util.List;

import dev.roanh.gmark.core.Configuration;
import dev.roanh.gmark.util.ConfigGraph;

/**
 * Represents the schema configuration of a graph.
 * This includes the set of predicates (edge labels),
 * node types (node labels) and the configuration
 * for how often edges with certain predicates are
 * present between nodes of a certain type.
 * @author Roan
 * @see Edge
 * @see Predicate
 * @see Type
 * @see Configuration
 */
public class Schema{
	/**
	 * List of schema edge configurations.
	 */
	private List<Edge> edges;
	/**
	 * List of all valid edge predicates.
	 */
	private List<Predicate> predicates;
	/**
	 * List of all valid node types.
	 */
	private List<Type> types;
	
	/**
	 * Constructs a new schema with the given set
	 * of edges, node types and predicates.
	 * @param edges The list of schema edges.
	 * @param types The list of node types.
	 * @param predicates The list of edge predicates.
	 */
	public Schema(List<Edge> edges, List<Type> types, List<Predicate> predicates){
		this.edges = edges;
		this.types = types;
		this.predicates = predicates;
	}
	
	/**
	 * Gets all predicates for this schema.
	 * @return All predicates (edge labels).
	 */
	public List<Predicate> getPredicates(){
		return predicates;
	}
	
	/**
	 * Gets all node types for this schema.
	 * @return All node types.
	 */
	public List<Type> getTypes(){
		return types;
	}
	
	/**
	 * Gets all edge configuration objects
	 * for this schema.
	 * @return All edge configuration objects.
	 */
	public List<Edge> getEdges(){
		return edges;
	}
	
	/**
	 * Gets the total number of distinct node
	 * types for this schema.
	 * @return The total number of node types.
	 * @see Type
	 */
	public int getTypeCount(){
		return types.size();
	}
	
	/**
	 * Gets the total number of distinct predicates
	 * for this schema. Note: this does not include
	 * inverse predicates.
	 * @return The total number of predicates.
	 * @see Predicate
	 */
	public int getPredicateCount(){
		return predicates.size();
	}
	
	/**
	 * Gets the total number of edge configurations
	 * in this schema.
	 * @return The total number of edge configurations.
	 * @see Edge
	 */
	public int getEdgeCount(){
		return edges.size();
	}
	
	/**
	 * Gets this schema in graph format where all types
	 * are nodes and all edge configurations edges between
	 * these type nodes.
	 * @return This schema in graph format.
	 */
	public ConfigGraph asGraph(){
		return new ConfigGraph(this);
	}
	
	/**
	 * Gets a type from this graph
	 * schema by its ID.
	 * @param id The ID of the type to get.
	 * @return The type with the given ID.
	 */
	public Type getType(int id){
		return types.get(id);
	}
	
	/**
	 * Gets a type from this graph schema
	 * by its textual alias name.
	 * @param alias The type alias.
	 * @return The type with the given alias.
	 */
	public Type getType(String alias){
		return types.stream().filter(t->t.getAlias().equals(alias)).findAny().orElse(null);
	}
	
	/**
	 * Gets a predicate from this graph
	 * schema by its ID.
	 * @param id The ID of the predicate to get.
	 * @return The predicate with the given ID.
	 */
	public Predicate getPredicate(int id){
		return predicates.get(id);
	}
	
	/**
	 * Gets a predicate from this graph schema
	 * by its textual alias name.
	 * @param alias The predicate alias.
	 * @return The predicate with the given alias.
	 */
	public Predicate getPredicate(String alias){
		return predicates.stream().filter(p->p.getAlias().equals(alias)).findAny().orElse(null);
	}
}
