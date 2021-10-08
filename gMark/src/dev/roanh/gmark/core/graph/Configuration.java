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
}
