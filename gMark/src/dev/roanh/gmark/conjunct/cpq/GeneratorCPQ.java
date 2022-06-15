/*
 * gMark: A domain- and query language-independent graph instance and query workload generator.
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
package dev.roanh.gmark.conjunct.cpq;

import java.util.List;
import java.util.stream.Collectors;

import dev.roanh.gmark.core.ConjunctGenerator;
import dev.roanh.gmark.core.graph.Predicate;
import dev.roanh.gmark.exception.GenerationException;
import dev.roanh.gmark.query.Conjunct;
import dev.roanh.gmark.query.Variable;
import dev.roanh.gmark.util.EdgeGraph;
import dev.roanh.gmark.util.EdgeGraphData;
import dev.roanh.gmark.util.Graph.GraphNode;
import dev.roanh.gmark.util.SchemaGraph;
import dev.roanh.gmark.util.SelectivityGraph;
import dev.roanh.gmark.util.SelectivityType;
import dev.roanh.gmark.util.Util;

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
	 * @see WorkloadCPQ
	 */
	public GeneratorCPQ(WorkloadCPQ wl){
		gs = new SchemaGraph(wl.getGraphSchema());
		workload = wl;
	}
	
	//never only id -- rules only count concat/intersect and can short circuit of course
	public static CPQ generatePlainCPQ(int ruleApplications, List<Predicate> labels){
		return generatePlainCPQ(ruleApplications, false, labels);
	}
	
	private static CPQ generatePlainCPQ(int ruleApplications, boolean allowId, List<Predicate> labels){
		if(ruleApplications == 1){
			if(allowId && Util.getRandom().nextBoolean()){
				return CPQ.IDENTITY;
			}else{
				//edge label
				Predicate label = Util.selectRandom(labels);
				return CPQ.label(Util.getRandom().nextBoolean() ? label : label.getInverse());
			}
		}else{
			if(Util.getRandom().nextBoolean()){
				//concat
				int split = Util.uniformRandom(1, ruleApplications - 2);
				return CPQ.concat(generatePlainCPQ(split, false, labels), generatePlainCPQ(ruleApplications - split - 1, false, labels));
			}else{
				//intersect
				int split = Util.uniformRandom(1, ruleApplications - 2);
				return CPQ.intersect(generatePlainCPQ(split, true, labels), generatePlainCPQ(ruleApplications - split - 1, true, labels));
			}
		}
	}

	@Override
	public Conjunct generateConjunct(SelectivityGraph gSel, SelectivityType start, SelectivityType end, Variable source, Variable target, boolean star) throws GenerationException{
		EdgeGraph graph = new EdgeGraph(gs, workload.getMaxDiameter(), start, end, workload.getMaxRecursion());
		List<GraphNode<EdgeGraphData, Void>> path = graph.drawPath();
		
		assert !path.isEmpty() : "path is not allowed not be empty!";
		
		return new ConjunctCPQ(path.size() == 1 ? path.get(0).getData().toCPQ() : new ConcatCPQ(
			path.stream().map(GraphNode::getData).map(EdgeGraphData::toCPQ).collect(Collectors.toList())
		), source, target, star);
	}
}
