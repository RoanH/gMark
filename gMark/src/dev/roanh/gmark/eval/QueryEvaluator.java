package dev.roanh.gmark.eval;

import java.util.Optional;

import dev.roanh.gmark.ast.QueryTree;

import nl.group9.quicksilver.core.data.PathQuery;

/**
 * Implementation of a simple reachability query evaluator. Note that for simplicity
 * and performance vertex, edge and label information is abstracted away and instead
 * associated with an integer.
 * @author Roan
 * @see <a href="https://research.roanh.dev/Graph%20Database%20&%20Query%20Evaluation%20Terminology%20v1.3.pdf">
 *      Graph Database &amp; Query Evaluation Terminology</a>
 */
public class QueryEvaluator{
	/**
	 * The main database graph.
	 */
	private final DatabaseGraph graph;
	
	public QueryEvaluator(DatabaseGraph graph){
		this.graph = graph;
	}

	public ResultGraph evaluate(PathQuery query){
		//see optimisation 2.6 & 2.11
		ResultGraph result = evaluate(query.query().toAbstractSyntaxTree());
		
		Optional<Integer> boundSource = query.source();
		if(boundSource.isPresent()){
			//see optimisation 2.3 & 2.13 
			result = result.selectSource(boundSource.get());
		}
		
		Optional<Integer> boundTarget = query.target();
		if(boundTarget.isPresent()){
			//see optimisation 2.4 & 2.14
			result = result.selectTarget(boundTarget.get());
		}
		
		return result;
	}

	/**
	 * Evaluates the given query tree (AST) bottom up.
	 * @param path The path query tree (AST) to evaluate.
	 * @return The result of evaluating the given query tree.
	 * @see QueryTree
	 */
	private ResultGraph evaluate(QueryTree path){
		switch(path.getOperation()){
		case CONCATENATION:
			return evaluate(path.getLeft()).join(evaluate(path.getRight()));
		case DISJUNCTION:
			return evaluate(path.getLeft()).union(evaluate(path.getRight()));
		case EDGE:
			return graph.selectLabel(path.getPredicate());
		case IDENTITY:
			return graph.selectIdentity();
		case INTERSECTION:
			return evaluate(path.getLeft()).intersection(evaluate(path.getRight()));
		case KLEENE:
			return evaluate(path.getLeft()).transitiveClosure();
		}
		
		throw new IllegalArgumentException("Unsupported database operation.");
	}
}
