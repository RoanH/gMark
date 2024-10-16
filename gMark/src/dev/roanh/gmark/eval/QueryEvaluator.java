package dev.roanh.gmark.eval;

import java.util.Optional;

import dev.roanh.gmark.ast.QueryTree;
import dev.roanh.gmark.core.graph.Predicate;
import dev.roanh.gmark.util.graph.BinaryGraph;

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
	 * After projection labels are no longer useful and generally make database operations
	 * hard or ambiguous to implement, so they are typically erased as soon as possible.
	 * <p>
	 * See optimisation 2.1.
	 */
	private static final int NO_LABEL = 0;
	/**
	 * The main database graph.
	 * <p>
	 * See optimisation 2.16.
	 */
	private SimpleGraph graph;

	public void prepare(SimpleGraph graph){
		//see optimisation (2.5), 2.16, 2.17 & (2.20)
		this.graph = graph;
	}
	
	public SimpleGraph evaluate(PathQuery query){
		//see optimisation 2.6 & 2.11
		SimpleGraph result = evaluate(query.query().toAbstractSyntaxTree());
		
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
	private ResultGraph evaluate(QueryTree path){
		//see optimisation (2.3), (2.4), 2.7, 2.9, 2.10 & 2.17
		switch(path.getOperation()){
		case CONCATENATION:
			return join(evaluate(path.getLeft()), evaluate(path.getRight()));
		case DISJUNCTION:
			return disjunction(evaluate(path.getLeft()), evaluate(path.getRight()));
		case EDGE:
			Predicate predicate = path.getPredicate();
			if(predicate.isInverse()){
				return selectInverseLabel(predicate.getID(), graph);
			}else{
				return selectLabel(predicate.getID(), graph);
			}
		case IDENTITY:
			return selectIdentity(graph);
		case INTERSECTION:
			return intersection(evaluate(path.getLeft()), evaluate(path.getRight()));
		case KLEENE:
			return transitiveClosure(evaluate(path.getLeft()));
		}
		
		throw new IllegalArgumentException("Unsupported database operation.");
	}
	
	/**
	 * Selects all the vertices from the given input database graph. Note that vertices
	 * are selected together with themselves to form a complete source target pair.
	 * @param in The input database graph to select vertices from.
	 * @return A copy of the input graph containing only vertices selected with themselves.
	 */
	private static BinaryGraph selectIdentity(SimpleGraph in){
		//see optimisation 2.1, (2.3), (2.4) & (2.9)
		SimpleGraph out = new SimpleGraph(in.getVertexCount(), in.getLabelCount());

		for(int vertex = 0; vertex < in.getVertexCount(); vertex++){
			out.addEdge(vertex, vertex, NO_LABEL);
		}
		
		return out;
	}
	
	/**
	 * Selects all edges from the given input database graph with the given label.
	 * @param projectLabel The projection label to use to filter the edge set.
	 * @param in The input database graph to select edges from.
	 * @return A copy of the input graph containing only edges with the given label.
	 */
	private static BinaryGraph selectLabel(int projectLabel, SimpleGraph in){
		//see optimisation 2.1, (2.3), (2.4), 2.5 & 2.20 
		SimpleGraph out = new SimpleGraph(in.getVertexCount(), in.getLabelCount());
		
		//follow edges going forward (natural direction)
		for(int source = 0; source < in.getVertexCount(); source++){
			for(TargetLabelPair edge : in.getOutgoingEdges(source)){
				if(edge.label() == projectLabel){
					out.addEdge(source, edge.target(), NO_LABEL);
				}
			}
		}
		
		return out;
	}
	
	/**
	 * Selects all inverse edges from the given input database graph with the given label.
	 * @param projectLabel The projection label to use to filter the edge set.
	 * @param in The input database graph to select inverted edges from.
	 * @return A copy of the input graph containing only inverted edges with the given label.
	 */
	private static BinaryGraph selectInverseLabel(int projectLabel, SimpleGraph in){
		//see optimisation 2.1, (2.3), (2.4), 2.5 & 2.20 
		SimpleGraph out = new SimpleGraph(in.getVertexCount(), in.getLabelCount());
		
		//follow edges going backward (from target to source)
		for(int target = 0; target < in.getVertexCount(); target++){
			for(SourceLabelPair edge : in.getIncomingEdges(target)){
				if(edge.label() == projectLabel){
					out.addEdge(target, edge.source(), NO_LABEL);
				}
			}
		}
		
		return out;
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
	 * Computes the disjunction (or union) of the given left and right input graphs.
	 * Recall that this operation simply added all the paths in both input graphs
	 * to the result graph. Note that the order of the input arguments is irrelevant.
	 * @param left The left input graph.
	 * @param right The right input graph.
	 * @return The result graph representing the union of the input graphs.
	 */
	private static BinaryGraph disjunction(SimpleGraph left, SimpleGraph right){
		//see optimisation 2.1, 2.5, 2.20, (2.21) & 2.22
		SimpleGraph out = new SimpleGraph(left.getVertexCount(), 1);
		
		//copy all edges in the left graph
		for(int source = 0; source < left.getVertexCount(); source++){
			for(TargetLabelPair edge : left.getOutgoingEdges(source)){
				out.addEdge(source, edge.target(), edge.label());
			}
		}
		
		//copy all edges in the right graph
		for(int source = 0; source < right.getVertexCount(); source++){
			for(TargetLabelPair edge : right.getOutgoingEdges(source)){
				out.addEdge(source, edge.target(), edge.label());
			}
		}
		
		return out;
	}
	
	/**
	 * Computes the intersection of the given left and right input graphs.
	 * Recall that this operation simply discards all paths that are not
	 * present in both input graphs. Note that the order of the input
	 * arguments is irrelevant.
	 * @param left The left input graph.
	 * @param right The right input graph.
	 * @return The result graph representing the intersection of the input graphs.
	 */
	private static BinaryGraph intersection(SimpleGraph left, SimpleGraph right){
		//see optimisation 2.1, 2.5, 2.19, 2.20 & (2.21)
		SimpleGraph out = new SimpleGraph(left.getVertexCount(), left.getLabelCount());
		
		for(int source = 0; source < left.getVertexCount(); source++){
			for(TargetLabelPair edge : left.getOutgoingEdges(source)){
				if(right.hasEdge(source, edge.target(), edge.label())){
					out.addEdge(source, edge.target(), edge.label());
				}
			}
		}
		
		return out;
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
