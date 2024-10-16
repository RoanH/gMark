package dev.roanh.gmark.eval;

import java.util.Optional;

import dev.roanh.gmark.ast.QueryTree;
import dev.roanh.gmark.core.graph.Predicate;
import dev.roanh.gmark.util.SmartBitSet;

import nl.group9.quicksilver.core.data.PathQuery;
import nl.group9.quicksilver.core.spec.DatabaseGraph;
import nl.group9.quicksilver.core.spec.Evaluator;
import nl.group9.quicksilver.impl.SimpleGraph;
import nl.group9.quicksilver.impl.data.SourceLabelPair;
import nl.group9.quicksilver.impl.data.TargetLabelPair;

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
	private final BinaryDatabaseGraph graph;
	private final SmartBitSet index;
	
	public QueryEvaluator(BinaryDatabaseGraph graph){
		this.graph = graph;
		index = new SmartBitSet(graph.getVertexCount());
	}

	public ResultGraph evaluate(PathQuery query){
		//see optimisation 2.6 & 2.11
		ResultGraph result = new ResultGraph(evaluate(query.query().toAbstractSyntaxTree()));
		
		Optional<Integer> boundSource = query.source();
		if(boundSource.isPresent()){
			//see optimisation 2.3 & 2.13 
			result = selectSource(boundSource.get(), result);
		}
		
		Optional<Integer> boundTarget = query.target();
		if(boundTarget.isPresent()){
			//see optimisation 2.4 & 2.14
			result = selectTarget(boundTarget.get(), result);
		}
		
		return result;
	}

	/**
	 * Evaluates the given query tree (AST) bottom up.
	 * @param path The path query tree (AST) to evaluate.
	 * @return The result of evaluating the given query tree.
	 * @see QueryTree
	 */
	private BinaryGraph evaluate(QueryTree path){
		switch(path.getOperation()){
		case CONCATENATION:
			return join(evaluate(path.getLeft()), evaluate(path.getRight()));
		case DISJUNCTION:
			return evaluate(path.getLeft()).union(evaluate(path.getRight()));
		case EDGE:
			return graph.selectLabel(path.getPredicate());
		case IDENTITY:
			return graph.selectIdentity();
		case INTERSECTION:
			return evaluate(path.getLeft()).intersection(evaluate(path.getRight()));
		case KLEENE:
			return transitiveClosure(evaluate(path.getLeft()));
		}
		
		throw new IllegalArgumentException("Unsupported database operation.");
	}
	
	/**
	 * Computes the transitive closure of the given input graph. Note that the transitive
	 * closure is the smallest graph that contains the entire input graph and is also transitive.
	 * @param in The input graph to compute the transitive closure of.
	 * @return A new graph representing the transitive closure of the input graph.
	 */
	private static BinaryGraph transitiveClosure(SimpleGraph in){
		//see optimisation 2.1, 2.5, 2.8, 2.20 & (2.21)
		SimpleGraph transitiveClosure = new SimpleGraph(in.getVertexCount(), in.getLabelCount());
		unionDistinct(transitiveClosure, in);
		
		int edgesAdded;
		do{
			edgesAdded = unionDistinct(transitiveClosure, join(transitiveClosure, in));
		}while(edgesAdded > 0);
		
		return transitiveClosure;
	}
	
	/**
	 * Computes the union of the given left and right input graphs by
	 * adding all edges from the right graph to the left graph that do
	 * not already exist in the left input graph.
	 * @param left The left input graph edges will be added to.
	 * @param right The right input graph containing edges to add.
	 * @return The number of new edges actually added to the left input graph.
	 * @see #disjunction(SimpleGraph, SimpleGraph)
	 */
	private static int unionDistinct(SimpleGraph left, SimpleGraph right){
		int edgesAdded = 0;
		
		for(int source = 0; source < right.getVertexCount(); source++){
			for(TargetLabelPair edge : right.getOutgoingEdges(source)){
				if(!left.hasEdge(source, edge.target(), edge.label())){
					left.addEdge(source, edge.target(), edge.label());
					edgesAdded++;
				}
			}
		}
		
		return edgesAdded;
	}
	
	
	/**
	 * Computes the join of the given left and right input graphs by
	 * extending paths in the left input graph with paths in the right
	 * input graph if the target vertex of a left input path is equal
	 * to the source vertex of a right input path.
	 * @param left The left input graph containing path prefixes.
	 * @param right The right input graph containing path suffixes.
	 * @return The result graph representing the join of the input graphs.
	 */
	private static BinaryGraph join(SimpleGraph left, SimpleGraph right){
		//see optimisation 2.1, 2.5, 2.18, 2.20 & (2.21)
		SimpleGraph out = new SimpleGraph(left.getVertexCount(), 1);
		
		for(int leftSource = 0; leftSource < left.getVertexCount(); leftSource++){
			for(TargetLabelPair firstEdge : left.getOutgoingEdges(leftSource)){
				//attempt to join this edge with all source vertices in the right graph
				for(TargetLabelPair secondEdge : right.getOutgoingEdges(firstEdge.target())){
					out.addEdge(leftSource, secondEdge.target(), NO_LABEL);
				}
			}
		}
		
		return out;
	}
	
	/**
	 * Selects all the edges from the given input graph that start at the given source node.
	 * @param source The source node of the edges.
	 * @param in The input graph to select edges from.
	 * @return A copy of the input graph containing only the edges that started at the given source vertex.
	 */
	private static BinaryGraph selectSource(int source, SimpleGraph in){
		//see optimisation 2.1, 2.3, 2.5, (2.20) & (2.21)
		SimpleGraph out = new SimpleGraph(in.getVertexCount(), in.getLabelCount());
		for(TargetLabelPair edge : in.getOutgoingEdges(source)){
			out.addEdge(source, edge.target(), edge.label());
		}
		
		return out;
	}
	
	/**
	 * Selects all the edges from the given input graph that end at the given target node.
	 * @param target The target node of the edges.
	 * @param in The input graph to select edges from.
	 * @return A copy of the input graph containing only the edges that ended at the given target vertex.
	 */
	private static BinaryGraph selectTarget(int target, SimpleGraph in){
		//see optimisation 2.1, 2.4, 2.5, (2.20) & (2.21)
		SimpleGraph out = new SimpleGraph(in.getVertexCount(), in.getLabelCount());
		for(SourceLabelPair edge : in.getIncomingEdges(target)){
			out.addEdge(edge.source(), target, edge.label());
		}
		
		return out;
	}
}
