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
package dev.roanh.gmark.gen.workload.cpq;

import java.util.List;

import dev.roanh.gmark.data.SelectivityType;
import dev.roanh.gmark.exception.GenerationException;
import dev.roanh.gmark.gen.workload.ConjunctGenerator;
import dev.roanh.gmark.lang.cpq.CPQ;
import dev.roanh.gmark.lang.cpq.ConcatCPQ;
import dev.roanh.gmark.query.Conjunct;
import dev.roanh.gmark.query.Variable;
import dev.roanh.gmark.util.graph.generic.UniqueGraph.GraphNode;
import dev.roanh.gmark.util.graph.specific.EdgeGraph;
import dev.roanh.gmark.util.graph.specific.EdgeGraphData;
import dev.roanh.gmark.util.graph.specific.SchemaGraph;
import dev.roanh.gmark.util.graph.specific.SelectivityGraph;

/**
 * Conjunct generator for CPQs (Conjunctive Path Queries).
 * @author Roan
 * @see CPQ
 */
public class ConjunctGeneratorCPQ implements ConjunctGenerator{
	/**
	 * The schema graph to use to generate CPQs.
	 */
	private SchemaGraph gs;
	/**
	 * The workload specifying what CPQs to generate.
	 */
	private WorkloadCPQ workload;
	
	/**
	 * Constructs a new CPQ conjunct generator using the given workload.
	 * @param wl The workload specification.
	 * @see WorkloadCPQ
	 */
	public ConjunctGeneratorCPQ(WorkloadCPQ wl){
		gs = new SchemaGraph(wl.getGraphSchema());
		workload = wl;
	}
	
	@Override
	public Conjunct generateConjunct(SelectivityGraph gSel, SelectivityType start, SelectivityType end, Variable source, Variable target, boolean star) throws GenerationException{
		EdgeGraph graph = new EdgeGraph(gs, workload.getMaxDiameter(), start, end, workload.getMaxRecursion());
		List<GraphNode<EdgeGraphData, Void>> path = graph.drawPath();
		
		assert !path.isEmpty() : "path is not allowed not be empty!";
		
		return new ConjunctCPQ(path.size() == 1 ? path.get(0).getData().toCPQ() : new ConcatCPQ(
			path.stream().map(GraphNode::getData).map(EdgeGraphData::toCPQ).toList()
		), source, target, star);
	}
}
