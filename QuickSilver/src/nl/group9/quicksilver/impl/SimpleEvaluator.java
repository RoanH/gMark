package nl.group9.quicksilver.impl;

import java.util.List;

import dev.roanh.gmark.ast.QueryTree;
import dev.roanh.gmark.core.graph.Predicate;

import nl.group9.quicksilver.core.data.CardStat;
import nl.group9.quicksilver.core.data.PathQuery;
import nl.group9.quicksilver.core.spec.Evaluator;
import nl.group9.quicksilver.impl.data.SourceLabelPair;
import nl.group9.quicksilver.impl.data.TargetLabelPair;

public class SimpleEvaluator implements Evaluator<SimpleGraph>{
	private SimpleGraph graph;

	@Override
	public SimpleGraph createGraph(int vertexCount, int edgeCount, int labelCount){//TODO used?
		return new SimpleGraph(vertexCount, labelCount);
	}

	@Override
	public void prepare(SimpleGraph graph){//TODO no cast
		this.graph = graph;
	}

	@Override
	public SimpleGraph evaluate(PathQuery query){
		SimpleGraph result = evaluate(query.ast());
		
		if(query.source().isPresent()){
			result = selectSource(query.source().get(), result);
		}
		
		if(query.target().isPresent()){
			result = selectedTarget(query.target().get(), result);
		}
		
		return result;
	}

	private SimpleGraph evaluate(QueryTree path){
		switch(path.getOperation()){
		case CONCATENATION:
			return join(evaluate(path.getLeft()), evaluate(path.getRight()));
		case DISJUNCTION:
			SimpleGraph left = evaluate(path.getLeft());
			unionDistinct(left, evaluate(path.getRight()));
			return left;
		case EDGE:
			Predicate predicate = path.getPredicate();
			return selectLabel(predicate.getID(), predicate.getID(), predicate.isInverse(), graph);
		case IDENTITY:
			//TODO
			break;
		case INTERSECTION:
			//TODO
			break;
		case KLEENE:
			//TODO tc in QuickSilver is simplified to a disjunction of labels, may need to reconsider that
			break;
		}
		
		throw new IllegalArgumentException("Unsupported database operation.");
	}
	
	@Override
	public CardStat computeCardinality(SimpleGraph graph){
		int outCount = 0;
		for(int source = 0; source < graph.getNoVertices(); source++){
			if(!graph.getOutgoingEdges(source).isEmpty()){
				outCount++;
			}
		}
		
		int inCount = 0;
		for(int target = 0; target < graph.getNoVertices(); target++){
			if(!graph.getIncomingEdges(target).isEmpty()){
				inCount++;
			}
		}
		
		return new CardStat(
			outCount,
			graph.getNoDistinctEdges(),
			inCount
		);
	}
	
	//TODO intersection and identity?
	
	private static SimpleGraph selectLabel(int projectLabel, int outLabel, boolean inverse, SimpleGraph in){
		SimpleGraph out = new SimpleGraph(in.getNoVertices(), in.getNoLabels());
		
		if(!inverse){
			//follow edges going forward (natural direction)
			for(int source = 0; source < in.getNoVertices(); source++){
				for(TargetLabelPair edge : in.getOutgoingEdges(source)){
					if(edge.label() == projectLabel){
						out.addEdge(source, edge.target(), outLabel);
					}
				}
			}
		}else{
			//follow edges going backward (from target to source)
			for(int source = 0; source < in.getNoVertices(); source++){
				for(SourceLabelPair edge : in.getIncomingEdges(source)){
					if(edge.label() == projectLabel){
						out.addEdge(source, edge.source(), outLabel);
					}
				}
			}
		}
		
		return out;
	}
	
	private static SimpleGraph transitiveClosure(List<Integer> labels, SimpleGraph in){
		SimpleGraph base = new SimpleGraph(in.getNoVertices(), in.getNoLabels());
		
		//construct the base graph containing only the edges with the labels we are interested in
		for(int label : labels){
			unionDistinct(base, selectLabel(label, 0, false, in));
		}
		
		SimpleGraph transitiveClosure = new SimpleGraph(in.getNoVertices(), in.getNoLabels());
		unionDistinct(transitiveClosure, base);
		
		int edgesAdded = 1;
		while(edgesAdded > 0){
			edgesAdded = unionDistinct(transitiveClosure, join(transitiveClosure, base));
		}
		
		return transitiveClosure;
	}
	
	private static int unionDistinct(SimpleGraph left, SimpleGraph right){
		int edgesAdded = 0;
		
		for(int source = 0; source < right.getNoVertices(); source++){
			for(TargetLabelPair edge : right.getOutgoingEdges(source)){
				if(!left.hasEdge(source, edge.target(), edge.label())){
					left.addEdge(source, edge.target(), source);
					edgesAdded++;
				}
			}
		}
		
		return edgesAdded;
	}
	
	private static SimpleGraph join(SimpleGraph left, SimpleGraph right){
		SimpleGraph out = new SimpleGraph(left.getNoVertices(), 1);
		
		for(int leftSource = 0; leftSource < left.getNoVertices(); leftSource++){
			for(TargetLabelPair firstEdge : left.getOutgoingEdges(leftSource)){
				//attempt to join this edge with all source vertices in the right graph
				for(TargetLabelPair secondEdge : right.getOutgoingEdges(firstEdge.target())){
					out.addEdge(leftSource, secondEdge.target(), 0);
				}
			}
		}
		
		return out;
	}
	
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
