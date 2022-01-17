package dev.roanh.gmark.core;

import java.util.HashSet;
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
	private int minArity;
	private int maxArity;
	private Set<QueryShape> shapes;//TODO assert this is not empty, general validation of everything really
	private Set<Selectivity> selectivities;//TODO assert this is not empty
	
	
	
	public int getMinArity(){
		return minArity;
	}
	
	public int getMaxArity(){
		return maxArity;
	}
	
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
	
	@Deprecated
	public static Workload getDummyInstance(){
		Workload wl = new Workload(){
		};
		
		wl.size = 1;
		wl.maxArity = 4;
		wl.minArity = 1;
		wl.maxLength = 6;
		wl.minLength = 1;//TODO not used for CPQs so move to be RPQ specific together with max length (use diameter for CPQ)
		wl.minConjuncts = 1;
		wl.maxConjuncts = 4;
		//TODO shapes
		Set<Selectivity> sel = new HashSet<Selectivity>();
		sel.add(Selectivity.LINEAR);
		sel.add(Selectivity.QUADRATIC);
		wl.selectivities = sel;
		
		return wl;
	}
}
