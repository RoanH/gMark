package dev.roanh.gmark.core.graph;

import java.util.List;

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
	
	public Schema(List<Edge> edges, List<Type> types, List<Predicate> predicates){
		this.edges = edges;
		this.types = types;
		this.predicates = predicates;
	}
	
	public List<Predicate> getPredicates(){
		return predicates;
	}
	
	public List<Type> getTypes(){
		return types;
	}
	
	public List<Edge> getEdges(){
		return edges;
	}
	
	public int getTypeCount(){
		return types.size();
	}
	
	public int getPredicateCount(){
		return predicates.size();
	}
	
	public int getEdgeCount(){
		return edges.size();
	}
}
