package dev.roanh.gmark.core.graph;

import java.util.List;

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
}
