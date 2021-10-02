package dev.roanh.gmark.impl.ucrpq;

import java.util.Set;

import dev.roanh.gmark.core.QueryShape;

/**
 * Describes a workload of UCRPQ queries to generate.
 * @author Roan
 */
public class UCRPQWorkload{
	/**
	 * Number of queries.
	 */
	private int size;
	/**
	 * Probability that a conjunct has a Kleene star above.
	 */
	private double multiplicity;
	private int minConjuncts;
	private int maxConjuncts;
	private int minDisjuncts;
	private int maxDisjuncts;
	private int minLength;
	private int maxLength;
	private int arity;
	private Set<QueryShape> shapes;
	//TODO selectivities
	
	
	
	
	
	
	public int getMinimumConjuncts(){
		return minConjuncts;
	}
	
	public int getMaximumConjuncts(){
		return maxConjuncts;
	}
	
	public Set<QueryShape> getShapes(){
		return shapes;
	}
}
