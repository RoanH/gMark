package nl.group9.quicksilver.core.spec;

import dev.roanh.gmark.eval.ResultGraph;

import nl.group9.quicksilver.core.data.PathQuery;

/**
 * Minimal specification for a query evaluator that operates on a fixed
 * database graph that does not get updated and evaluates reachability
 * path queries on this database graph.
 * @author Roan
 * @param <G> The input database graph type.
 * @param <R> The output result graph type.
 * @see DatabaseGraph
 * @see ResultGraph
 * @see PathQuery
 */
public abstract interface Evaluator<G extends DatabaseGraph, R extends ResultGraph>{

	/**
	 * Prepares this evaluator to evaluate queries on the given database graph.
	 * Any preparation for query evaluation, like index creation and estimator
	 * preparation, should be done in this method.
	 * @param graph The database graph to evaluate queries on.
	 * @see DatabaseGraph
	 */
	public abstract void prepare(G graph);
	
	/**
	 * Evaluates the given reachability path query on the database graph for
	 * this evaluator and returns the result graph.
	 * @param query The path query to evaluate.
	 * @return The query answer result graph containing the matched paths.
	 * @see PathQuery
	 * @see ResultGraph
	 */
	public abstract R evaluate(PathQuery query);
}
