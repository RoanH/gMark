package dev.roanh.gmark.core;

import java.util.Set;

//TODO need to redo gmark formats a bit, probably just add another param after workload size with type="cpq", rpq, etc keep this class for shared info

public abstract class Workload{
	/**
	 * Number of queries.
	 */
	private int size;
	private int minConjuncts;
	private int maxConjuncts;
	private int minLength;
	private int maxLength;
	private int arity;
	private Set<QueryShape> shapes;
	private Set<Selectivity> selectivities;
	
	
	
	
	public int getMaxLength(){
		return maxLength;
	}
	
	public int getMinLength(){
		return minLength;
	}
	
	public int getMinConjuncts(){
		return minConjuncts;
	}
	
	public int getMaxConjuncts(){
		return maxConjuncts;
	}
	
	public Set<QueryShape> getShapes(){
		return shapes;
	}
	
	public Set<Selectivity> getSelectivities(){
		return selectivities;
	}
}
