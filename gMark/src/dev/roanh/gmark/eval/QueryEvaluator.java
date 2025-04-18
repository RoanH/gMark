/*
 * gMark: A domain- and query language-independent query workload generator and query language utility library.
 * Copyright (C) 2021  Roan Hofland (roan@roanh.dev).  All rights reserved.
 * GitHub Repository: https://github.com/RoanH/gMark
 *
 * gMark is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * gMark is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package dev.roanh.gmark.eval;

import java.util.List;

import dev.roanh.gmark.ast.OperationType;
import dev.roanh.gmark.ast.QueryTree;
import dev.roanh.gmark.type.schema.Predicate;
import dev.roanh.gmark.util.graph.generic.IntGraph;

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
public class QueryEvaluator{//reachability query evaluator?
	/**
	 * Constant used to indicate an unbound (free) query source and/or target vertex.
	 */
	private static final int UNBOUND = -1;
	/**
	 * The main database graph.
	 */
	private final DatabaseGraph graph;
	
	/**
	 * Constructs a new query evaluator for the given database graph.
	 * @param graph The database graph to evaluate queries on.
	 */
	public QueryEvaluator(IntGraph graph){
		this(new DatabaseGraph(graph));
	}
	
	/**
	 * Constructs a new query evaluator for the given database graph.
	 * @param graph The database graph to evaluate queries on.
	 */
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
	 * @param source The ID of the bound source vertex, or -1 if unbound.
	 * @param path The path query tree (AST) to evaluate.
	 * @param target The ID of the bound target vertex, or -1 if unbound.
	 * @return The result of evaluating the given query tree.
	 * @see QueryTree
	 */
	private ResultGraph evaluate(int source, QueryTree path, int target){
		switch(path.getOperation()){
		case CONCATENATION:
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
	
	/**
	 * Plans the evaluation of an intersection operation. Notably triggers
	 * special handling for intersection with identity, where evaluation of
	 * the intersection operation is skipped and instead all vertices with
	 * self loops are selected from the input graph.
	 * @param source The ID of the bound source vertex, or -1 if unbound.
	 * @param path The path query tree (AST) to evaluate.
	 * @param target The ID of the bound target vertex, or -1 if unbound.
	 * @return The result of evaluating the given query tree.
	 * @see OperationType#IDENTITY
	 * @see OperationType#INTERSECTION
	 * @see ResultGraph#selectIdentity()
	 * @see ResultGraph#intersection(ResultGraph)
	 */
	private ResultGraph planIntersection(int source, QueryTree path, int target){
		if(path.getLeft().getOperation() == OperationType.IDENTITY){
			return evaluate(source, path.getRight(), target).selectIdentity();
		}else if(path.getRight().getOperation() == OperationType.IDENTITY){
			return evaluate(source, path.getLeft(), target).selectIdentity();
		}else{
			return evaluate(source, path.getLeft(), target).intersection(evaluate(source, path.getRight(), target));
		}
	}
	
	/**
	 * Plans the evaluation of a transitive closure operation. This planner
	 * selected an appropriate transitive closure implementation depending
	 * on whether the source and/or target vertices are bound or not.
	 * @param source The ID of the bound source vertex, or -1 if unbound.
	 * @param path The path query tree (AST) to evaluate.
	 * @param target The ID of the bound target vertex, or -1 if unbound.
	 * @return The result of evaluating the given query tree.
	 * @see OperationType#KLEENE
	 * @see ResultGraph#transitiveClosure()
	 */
	private ResultGraph planTransitiveClosure(int source, QueryTree path, int target){
		ResultGraph base = evaluate(UNBOUND, path.getLeft(), UNBOUND);
		
		if(source == UNBOUND){
			return target == UNBOUND ? base.transitiveClosure() : base.transitiveClosureTo(target);
		}else{
			return target == UNBOUND ? base.transitiveClosureFrom(source) : base.transitiveClosure(source, target);
		}
	}
	
	/**
	 * Selects identity from the database graph (i.e., zero length paths).
	 * @param source The ID of the bound source vertex, or -1 if unbound.
	 * @param target The ID of the bound target vertex, or -1 if unbound.
	 * @return The result of selecting identity from the database graph.
	 * @see OperationType#IDENTITY
	 * @see DatabaseGraph#selectIdentity()
	 */
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
	
	/**
	 * Selects all edges with the given predicate from the database graph (i.e., length one paths).
	 * @param source The ID of the bound source vertex, or -1 if unbound.
	 * @param label The label of the edges to selected (predicate).
	 * @param target The ID of the bound target vertex, or -1 if unbound.
	 * @return The result of selecting all edge with the requested label from the database graph.
	 * @see OperationType#EDGE
	 * @see DatabaseGraph#selectLabel(Predicate)
	 */
	private ResultGraph selectEdge(int source, Predicate label, int target){
		if(source == UNBOUND){
			return target == UNBOUND ? graph.selectLabel(label) : graph.selectLabel(label, target);
		}else{
			return target == UNBOUND ? graph.selectLabel(source, label) : graph.selectLabel(source, label, target);
		}
	}
	
	/**
	 * Gets a list of all labels for the graph this evaluator works with.
	 * @return All labels for the database graph.
	 */
	public List<Predicate> getLabels(){
		return graph.getLabels();
	}
}
