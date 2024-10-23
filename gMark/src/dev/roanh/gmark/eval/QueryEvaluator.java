package dev.roanh.gmark.eval;

import dev.roanh.gmark.ast.OperationType;
import dev.roanh.gmark.ast.QueryTree;
import dev.roanh.gmark.core.graph.Predicate;

/**
 * Implementation of a simple reachability query evaluator. Note that for simplicity
 * and performance vertex, edge and label information is abstracted away and instead
 * associated with an integer.
 * @author Roan
 * @see <a href="https://research.roanh.dev/Graph%20Database%20&%20Query%20Evaluation%20Terminology%20v1.3.pdf">
 *      Graph Database &amp; Query Evaluation Terminology</a>
 * @see <a href="https://research.roanh.dev/Optimising%20a%20Simple%20Graph%20Database%20v1.1.pdf">
 *      Optimising a Simple Graph Database</a>
 * @see DatabaseGraph
 * @see ResultGraph
 */
public class QueryEvaluator{
	private static final int UNBOUND = -1;
	/**
	 * The main database graph.
	 */
	private final DatabaseGraph graph;
	
	public QueryEvaluator(DatabaseGraph graph){
		this.graph = graph;
	}

	/**
	 * Evaluates the given reachability path query on the database graph for
	 * this evaluator and returns the result graph.
	 * @param query The path query to evaluate.
	 * @return The query answer result graph containing the matched paths.
	 * @see PathQuery
	 * @see ResultGraph
	 */
	public ResultGraph evaluate(PathQuery query){
		return evaluate(
			query.source().orElse(UNBOUND),
			query.query().toAbstractSyntaxTree(),
			query.target().orElse(UNBOUND)
		);
	}

	/**
	 * Evaluates the given query tree (AST) bottom up.
	 * @param path The path query tree (AST) to evaluate.
	 * @return The result of evaluating the given query tree.
	 * @see QueryTree
	 */
	private ResultGraph evaluate(int source, QueryTree path, int target){
		switch(path.getOperation()){
		case CONCATENATION://TODO simple join planner?
			return evaluate(source, path.getLeft(), UNBOUND).join(evaluate(UNBOUND, path.getRight(), target));
		case DISJUNCTION:
			return evaluate(source, path.getLeft(), target).union(evaluate(source, path.getRight(), target));
		case EDGE:
			return selectEdge(source, path.getPredicate(), target);
		case IDENTITY:
			return selectIdentity(source, target);
		case INTERSECTION:
			return planIntersection(source, path, target);
		case KLEENE:
			return planTransitiveClosure(source, path, target);
		}

		throw new IllegalArgumentException("Unsupported database operation.");
	}
	
	//notably identity
	private ResultGraph planIntersection(int source, QueryTree path, int target){
		if(path.getLeft().getOperation() == OperationType.IDENTITY){
			return evaluate(source, path.getRight(), target).selectIdentity();
		}else if(path.getRight().getOperation() == OperationType.IDENTITY){
			return evaluate(source, path.getLeft(), target).selectIdentity();
		}else{
			return evaluate(source, path.getLeft(), target).intersection(evaluate(source, path.getRight(), target));
		}
	}
	
	private ResultGraph planTransitiveClosure(int source, QueryTree path, int target){
		ResultGraph base = evaluate(UNBOUND, path.getLeft(), UNBOUND);
		
		if(source == UNBOUND){
			return target == UNBOUND ? base.transitiveClosure() : base.transitiveClosureTo(target);
		}else{
			return target == UNBOUND ? base.transitiveClosureFrom(source) : base.transitiveClosure(source, target);
		}
	}
	
	private ResultGraph selectIdentity(int source, int target){
		if(source == UNBOUND){
			return target == UNBOUND ? graph.selectIdentity() : graph.selectIdentity(target);
		}else{
			if(target == UNBOUND){
				return graph.selectIdentity(source);
			}else{
				return source == target ? graph.selectIdentity(source) : ResultGraph.empty(graph.getVertexCount());
			}
		}
	}
	
	private ResultGraph selectEdge(int source, Predicate label, int target){
		if(source == UNBOUND){
			return target == UNBOUND ? graph.selectLabel(label) : graph.selectLabel(label, target);
		}else{
			return target == UNBOUND ? graph.selectLabel(source, label) : graph.selectLabel(source, label, target);
		}
	}
}
