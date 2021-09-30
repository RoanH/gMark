package dev.roanh.gmark.core;

/**
 * Enum of all the selectivity classes. At its core this describes
 * the effect of an edge between two nodes of a given type. There
 * are two primary factors that influence the selectivity class:
 * <ol>
 * <li>The presence of the source and target node in the graph. That
 * is whether the nodes are present in a fixed quantity (constant) or
 * in a quantity that grows with the size of the graph (growing).</li>
 * <li>The incoming and outgoing distribution of the edge</li>
 * </ol>
 * @author Roan
 */
public enum SelectivityClass{
	/**
	 * <code>(1,=,1)</code> a class between constant
	 * nodes, also the only class associated with a
	 * query of constant selectivity.
	 */
	ONE_ONE,
	/**
	 * <code>(N,>,1)</code> a class between a growing
	 * and a constant node, associated with a query
	 * of linear selectivity.
	 */
	N_ONE,
	/**
	 * <code>(1,<,N)</code> a class between a growing
	 * and a constant node, associated with a query
	 * of linear selectivity.
	 */
	ONE_N,
	/**
	 * <code>(N,=,N)</code> a class between growing
	 * nodes, associated with a query of linear selectivity.
	 */
	EQUALS,
	/**
	 * <code>(N,>,N)</code> a class between growing
	 * nodes, where the edge in distribution is zipfian.
	 * Associated with a query of linear selectivity.
	 */
	GREATER,
	/**
	 * <code>(N,<,N)</code> a class between growing
	 * nodes, where the edge out distribution is zipfian.
	 * Associated with a query of linear selectivity.
	 */
	LESS,
	/**
	 * <code>(N,◇,N)</code> a class between growing
	 * nodes where the edge in and out distributions
	 * are both zipfian. Associated with a query of
	 * linear selectivity.
	 */
	LESS_GREATER,
	/**
	 * <code>(N,⨉,N)</code> a class between growing
	 * nodes, this includes for example the transitive
	 * closure. This class is always derived from the
	 * combination of the other selectivity classes.
	 * This is also the only class associated with a
	 * query of quadratic selectivity.
	 */
	CROSS
}
