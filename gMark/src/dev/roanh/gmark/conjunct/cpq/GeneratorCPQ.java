package dev.roanh.gmark.conjunct.cpq;

import java.util.List;

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

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	private static final Conjunct generateInnerCPQ(SelectivityGraph g, SchemaGraph gs, SelectivityType source, SelectivityType target, int maxLength, int maxRecursion){
		EdgeGraph graph = new EdgeGraph(gs, maxLength, source, target, maxRecursion);
		List<GraphNode<EdgeGraphData, Void>> path = graph.drawPath();
		
		
		
		
		return null;//TODO
	}
}
