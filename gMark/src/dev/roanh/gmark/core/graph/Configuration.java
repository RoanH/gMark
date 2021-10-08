package dev.roanh.gmark.core.graph;

import java.util.List;

import dev.roanh.gmark.core.Workload;

//basically a configuration tells gMark what to generate
public class Configuration{
	//lists with the sizes (in number of nodes) of the graphs to generate
	private List<Integer> graphs;
	/**
	 * List of all valid edge predicates.
	 */
	private List<Predicate> predicates;
	/**
	 * List of all valid node types.
	 */
	private List<Type> types;
	/**
	 * The schema of the graphs to generate.
	 */
	private Schema schema;
	/**
	 * List of workloads to generate.
	 */
	private List<Workload> workloads;
	
	public Configuration(List<Integer> sizes, List<Predicate> predicates, List<Type> types, Schema schema, List<Workload> workloads){
		this.graphs = sizes;
		this.predicates = predicates;
		this.types = types;
		this.schema = schema;
		this.workloads = workloads;
	}
	
	public List<Predicate> getPredicates(){
		return predicates;
	}
	
	public List<Type> getTypes(){
		return types;
	}
}
