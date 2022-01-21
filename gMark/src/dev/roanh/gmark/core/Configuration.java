package dev.roanh.gmark.core;

import java.util.List;

import dev.roanh.gmark.core.graph.Predicate;
import dev.roanh.gmark.core.graph.Schema;
import dev.roanh.gmark.core.graph.Type;

//basically a configuration tells gMark what to generate
public class Configuration{
	//lists with the sizes (in number of nodes) of the graphs to generate
	private List<Integer> graphs;
	/**
	 * The schema of the graphs to generate.
	 */
	private Schema schema;
	/**
	 * List of workloads to generate.
	 */
	private List<Workload> workloads;
	
	public Configuration(List<Integer> sizes, Schema schema, List<Workload> workloads){
		this.graphs = sizes;
		this.schema = schema;
		this.workloads = workloads;
	}
	
	public List<Predicate> getPredicates(){
		return schema.getPredicates();
	}
	
	public List<Type> getTypes(){
		return schema.getTypes();
	}
	
	public Schema getSchema(){
		return schema;
	}
	
	public List<Workload> getWorkloads(){
		return workloads;
	}
	
	public Workload getWorkloadByID(int id){
		for(Workload wl : workloads){
			if(wl.getID() == id){
				return wl;
			}
		}
		return null;
	}
}
