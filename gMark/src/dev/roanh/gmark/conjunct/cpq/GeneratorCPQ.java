package dev.roanh.gmark.conjunct.cpq;

import java.util.List;
import java.util.stream.Collectors;

import dev.roanh.gmark.exception.GenerationException;
import dev.roanh.gmark.query.Conjunct;
import dev.roanh.gmark.util.EdgeGraph;
import dev.roanh.gmark.util.EdgeGraphData;
import dev.roanh.gmark.util.Graph.GraphNode;
import dev.roanh.gmark.util.SchemaGraph;
import dev.roanh.gmark.util.SelectivityGraph;
import dev.roanh.gmark.util.SelectivityType;

/**
 * Generator for CPQ (Conjunctive Path Queries).
 * @author Roan
 */
public class GeneratorCPQ{

	public static final Conjunct generateInnerCPQ(SelectivityGraph g, SchemaGraph gs, SelectivityType source, SelectivityType target, int maxLength, int maxRecursion) throws GenerationException{
		EdgeGraph graph = new EdgeGraph(gs, maxLength, source, target, maxRecursion);
		List<GraphNode<EdgeGraphData, Void>> path = graph.drawPath();
		
		assert !path.isEmpty() : "path may not be empty!";
		
		return new ConjunctCPQ(path.size() == 1 ? path.get(0).getData().toCPQ() : new ConcatCPQ(
			path.stream().map(GraphNode::getData).map(EdgeGraphData::toCPQ).collect(Collectors.toList())
		));
	}
}
