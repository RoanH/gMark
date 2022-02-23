package dev.roanh.gmark.core;

import java.util.List;

import dev.roanh.gmark.ConfigParser;
import dev.roanh.gmark.core.graph.Predicate;
import dev.roanh.gmark.core.graph.Schema;
import dev.roanh.gmark.core.graph.Type;

/**
 * A configuration contains all the information on the graphs
 * and workloads gMark needs to generate and is typically
 * read from a configuration XML file.
 * @author Roan
 * @see ConfigParser
 * @see Workload
 * @see Schema
 */
public class Configuration{
	/**
	 * A list of sizes (node count) of the graphs to generate.
	 */
	private List<Integer> graphs;
	/**
	 * The schema of the graphs to generate
	 * and to use to generate queries.
	 */
	private Schema schema;
	/**
	 * List of workloads to generate.
	 */
	private List<Workload> workloads;
	
	/**
	 * Constructs a new gMark configuration.
	 * @param sizes The sizes (in nodes) of the graphs to generate.
	 * @param schema The schema to use to generate the graphs and queries.
	 * @param workloads The workloads to generate.
	 */
	public Configuration(List<Integer> sizes, Schema schema, List<Workload> workloads){
		this.graphs = sizes;
		this.schema = schema;
		this.workloads = workloads;
	}
	
	/**
	 * Gets a list of the graph sizes to generate.
	 * The graph sizes returned are in the number of nodes.
	 * @return The graph sizes to generate.
	 */
	public List<Integer> getGraphSizes(){
		return graphs;
	}
	
	/**
	 * Gets a list of all the predicates in the
	 * graph schema for this configuration.
	 * @return A list of all predicates for
	 *         the schema in this configuration.
	 */
	public List<Predicate> getPredicates(){
		return schema.getPredicates();
	}
	
	/**
	 * Gets a list of all the node types in the
	 * graph schema for this configuration.
	 * @return A list of all node types for
	 *         the schema in this configuration.
	 */
	public List<Type> getTypes(){
		return schema.getTypes();
	}
	
	/**
	 * Gets the graph schema for this configuration.
	 * @return The graph schema for this configuration.
	 */
	public Schema getSchema(){
		return schema;
	}
	
	/**
	 * Gets all the workloads in this configuration.
	 * @return All the workloads in this configuration.
	 */
	public List<Workload> getWorkloads(){
		return workloads;
	}
	
	/**
	 * Gets the workload with the given ID from this configuration.
	 * @param id The ID of the workload to get.
	 * @return The workload with the given ID or
	 *         <code>null</code> if it doesn't exist.
	 */
	public Workload getWorkloadByID(int id){
		for(Workload wl : workloads){
			if(wl.getID() == id){
				return wl;
			}
		}
		return null;
	}
}
