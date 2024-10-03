package nl.group9.quicksilver.impl;

import java.util.Optional;

import dev.roanh.gmark.ast.QueryTree;
import dev.roanh.gmark.core.graph.Predicate;

import nl.group9.quicksilver.core.data.PathQuery;
import nl.group9.quicksilver.core.spec.Evaluator;
import nl.group9.quicksilver.impl.data.SourceLabelPair;
import nl.group9.quicksilver.impl.data.TargetLabelPair;

/**
 * Implementation of a simple reachability query evaluator.
 * @author Roan
 * @see <a href="https://research.roanh.dev/Graph%20Database%20&%20Query%20Evaluation%20Terminology%20v1.3.pdf">
 *      Graph Database &amp; Query Evaluation Terminology</a>
 */
public class SimpleEvaluator implements Evaluator<SimpleGraph, SimpleGraph>{
	/**
	 * After projection labels are no longer useful and generally make database operations
	 * hard or ambiguous to implement, so they are typically erased as soon as possible.
	 */
	private static final int NO_LABEL = 0;//TODO I did not erase them entirely
	private SimpleGraph graph;

	@Override
	public void prepare(SimpleGraph graph){
		this.graph = graph;
	}
	
	//TODO inefficiencies (not meant to be solved)
	//- almost all operations produce a new graph, a lot can be significantly faster if they modify the input graph
	//- prevent duplicate edges from ending up in output graphs as much as possible
	//- some operations are just more efficient when considered in more context, e.g., intersection with identity
	//- labels are only used for projection, after that they are only a huge inefficiency

	@Override
	public SimpleGraph evaluate(PathQuery query){
		SimpleGraph result = evaluate(query.query().toAbstractSyntaxTree());
		
		Optional<Integer> boundSource = query.source();
		if(boundSource.isPresent()){
			result = selectSource(boundSource.get(), result);
		}
		
		Optional<Integer> boundTarget = query.target();
		if(boundTarget.isPresent()){
			result = selectedTarget(boundTarget.get(), result);
		}
		
		return result;
	}

	private SimpleGraph evaluate(QueryTree path){
		switch(path.getOperation()){
		case CONCATENATION:
			return join(evaluate(path.getLeft()), evaluate(path.getRight()));
		case DISJUNCTION:
			return union(evaluate(path.getLeft()), evaluate(path.getRight()));
		case EDGE:
			Predicate predicate = path.getPredicate();
			return selectLabel(predicate.getID(), predicate.isInverse(), graph);
		case IDENTITY:
			return selectIdentity(graph);
		case INTERSECTION:
			return intersection(evaluate(path.getLeft()), evaluate(path.getRight()));
		case KLEENE:
			return transitiveClosure(evaluate(path.getLeft()));
		}
		
		throw new IllegalArgumentException("Unsupported database operation.");
	}
	
	private static SimpleGraph selectIdentity(SimpleGraph in){
		SimpleGraph out = new SimpleGraph(in.getNoVertices(), in.getNoLabels());

		for(int vertex = 0; vertex < in.getNoVertices(); vertex++){
			out.addEdge(vertex, vertex, NO_LABEL);
		}
		
		return out;
	}
	
	private static SimpleGraph selectLabel(int projectLabel, boolean inverse, SimpleGraph in){
		SimpleGraph out = new SimpleGraph(in.getNoVertices(), in.getNoLabels());
		
		if(!inverse){
			//follow edges going forward (natural direction)
			for(int source = 0; source < in.getNoVertices(); source++){
				for(TargetLabelPair edge : in.getOutgoingEdges(source)){
					if(edge.label() == projectLabel){
						out.addEdge(source, edge.target(), NO_LABEL);
					}
				}
			}
		}else{
			//follow edges going backward (from target to source)
			for(int source = 0; source < in.getNoVertices(); source++){
				for(SourceLabelPair edge : in.getIncomingEdges(source)){
					if(edge.label() == projectLabel){
						out.addEdge(source, edge.source(), NO_LABEL);
					}
				}
			}
		}
		
		return out;
	}
	
	private static SimpleGraph transitiveClosure(SimpleGraph in){
		SimpleGraph transitiveClosure = new SimpleGraph(in.getNoVertices(), in.getNoLabels());
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
	 * @see #union(SimpleGraph, SimpleGraph)
	 */
	private static int unionDistinct(SimpleGraph left, SimpleGraph right){
		int edgesAdded = 0;
		
		for(int source = 0; source < right.getNoVertices(); source++){
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
	 * Computes the union (or disjunction) of the given left and right input graphs.
	 * Recall that this operation simply added all the paths in both input graphs
	 * to the result graph. Note that the order of the input arguments is irrelevant.
	 * @param left The left input graph.
	 * @param right The right input graph.
	 * @return The result graph representing the union of the input graphs.
	 */
	private static SimpleGraph union(SimpleGraph left, SimpleGraph right){
		SimpleGraph out = new SimpleGraph(left.getNoVertices(), 1);
		
		//copy all edges in the left graph
		for(int source = 0; source < left.getNoVertices(); source++){
			for(TargetLabelPair edge : left.getOutgoingEdges(source)){
				out.addEdge(source, edge.target(), edge.label());
			}
		}
		
		//copy all edges in the right graph
		for(int source = 0; source < right.getNoVertices(); source++){
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
	private static SimpleGraph intersection(SimpleGraph left, SimpleGraph right){
		SimpleGraph out = new SimpleGraph(left.getNoVertices(), left.getNoLabels());
		
		for(int source = 0; source < left.getNoVertices(); source++){
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
	private static SimpleGraph join(SimpleGraph left, SimpleGraph right){
		SimpleGraph out = new SimpleGraph(left.getNoVertices(), 1);
		
		for(int leftSource = 0; leftSource < left.getNoVertices(); leftSource++){
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
	private static SimpleGraph selectSource(int source, SimpleGraph in){
		SimpleGraph out = new SimpleGraph(in.getNoVertices(), in.getNoLabels());
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
	private static SimpleGraph selectedTarget(int target, SimpleGraph in){
		SimpleGraph out = new SimpleGraph(in.getNoVertices(), in.getNoLabels());
		for(SourceLabelPair edge : in.getIncomingEdges(target)){
			out.addEdge(edge.source(), target, edge.label());
		}
		
		return out;
	}
}
