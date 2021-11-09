package dev.roanh.gmark.conjunct.rpq;

import dev.roanh.gmark.core.Workload;

/**
 * Describes a workload of UCRPQ queries to generate.
 * @author Roan
 */
@Deprecated
public class RPQWorkload extends Workload{
	/**
	 * Probability that a conjunct has a Kleene star above.
	 */
	private double multiplicity;
	private int minDisjuncts;
	private int maxDisjuncts;
}
