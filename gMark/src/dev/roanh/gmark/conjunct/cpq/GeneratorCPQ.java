package dev.roanh.gmark.conjunct.cpq;

import java.util.List;
import java.util.stream.Collectors;

import dev.roanh.gmark.core.ConjunctGenerator;
import dev.roanh.gmark.core.Workload;
import dev.roanh.gmark.exception.GenerationException;
import dev.roanh.gmark.query.Conjunct;
import dev.roanh.gmark.util.EdgeGraph;
import dev.roanh.gmark.util.EdgeGraphData;
import dev.roanh.gmark.util.Graph.GraphNode;
import dev.roanh.gmark.util.SchemaGraph;
import dev.roanh.gmark.util.SelectivityGraph;
import dev.roanh.gmark.util.SelectivityType;

/**
 * Generator for CPQs (Conjunctive Path Queries).
 * @author Roan
 */
public class GeneratorCPQ implements ConjunctGenerator{
	/**
	 * The schema graph to use to generate CPQs.
	 */
	private SchemaGraph gs;
	/**
	 * The workload specifying what CPQs to generate.
	 */
	private WorkloadCPQ workload;
	
	/**
	 * Constructs a new CPQ generator using the given workload.
	 * @param wl The workload specification.
	 * @throws IllegalArgumentException When the given workload
	 *         specification is not of the type {@link WorkloadCPQ}.
	 * @see WorkloadCPQ
	 */
	public GeneratorCPQ(Workload wl) throws IllegalArgumentException{
		if(!(wl instanceof WorkloadCPQ)){
			throw new IllegalArgumentException("Incorret workload type.");
		}
		
		gs = new SchemaGraph(wl.getGraphSchema());
		workload = (WorkloadCPQ)wl;
	}

	@Override
	public Conjunct generateConjunct(SelectivityGraph gSel, SelectivityType start, SelectivityType end) throws GenerationException{
		EdgeGraph graph = new EdgeGraph(gs, workload.getMaxDiameter(), start, end, workload.getMaxRecursion());
		List<GraphNode<EdgeGraphData, Void>> path = graph.drawPath();
		
		assert !path.isEmpty() : "path is not allowed not be empty!";
		
		return new ConjunctCPQ(path.size() == 1 ? path.get(0).getData().toCPQ() : new ConcatCPQ(
			path.stream().map(GraphNode::getData).map(EdgeGraphData::toCPQ).collect(Collectors.toList())
		));
	}
}
