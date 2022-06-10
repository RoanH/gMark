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
package dev.roanh.gmark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import dev.roanh.gmark.conjunct.cpq.WorkloadCPQ;
import dev.roanh.gmark.conjunct.rpq.WorkloadRPQ;
import dev.roanh.gmark.core.Configuration;
import dev.roanh.gmark.core.DistributionType;
import dev.roanh.gmark.core.QueryShape;
import dev.roanh.gmark.core.Selectivity;
import dev.roanh.gmark.core.Workload;
import dev.roanh.gmark.core.WorkloadType;
import dev.roanh.gmark.core.graph.Edge;
import dev.roanh.gmark.exception.ConfigException;

public class ConfigParserTest{
	private static Configuration config;

	@BeforeAll
	public static void parseConfig(){
		try{
			config = ConfigParser.parse(ClassLoader.getSystemResourceAsStream("test.xml"));
		}catch(ConfigException e){
			e.printStackTrace();
		}
	}
	
	@Test
	public void legacyWorkloadRPQ(){
		Workload workload = config.getWorkloadByID(0);
		
		assertEquals(WorkloadType.RPQ, workload.getType());
		assertTrue(workload instanceof WorkloadRPQ);
		
		WorkloadRPQ wl = (WorkloadRPQ)workload;
		assertEquals(1, wl.getMinDisjuncts());
		assertEquals(3, wl.getMaxDisjuncts());
		assertEquals(2, wl.getMinLength());
		assertEquals(4, wl.getMaxLength());
	}
	
	@Test
	public void workloadRPQ(){
		Workload workload = config.getWorkloadByID(2);
		
		assertEquals(WorkloadType.RPQ, workload.getType());
		assertTrue(workload instanceof WorkloadRPQ);
		
		WorkloadRPQ wl = (WorkloadRPQ)workload;
		assertEquals(2, wl.getMinDisjuncts());
		assertEquals(4, wl.getMaxDisjuncts());
		assertEquals(1, wl.getMinLength());
		assertEquals(3, wl.getMaxLength());
	}
	
	@Test
	public void workloadCPQ(){
		Workload workload = config.getWorkloadByID(1);
		
		assertEquals(WorkloadType.CPQ, workload.getType());
		assertTrue(workload instanceof WorkloadCPQ);
		
		WorkloadCPQ wl = (WorkloadCPQ)workload;
		assertEquals(5, wl.getMaxRecursion());
		assertEquals(4, wl.getMaxDiameter());
	}
	
	@Test
	public void workload(){
		Workload workload = config.getWorkloadByID(2);
		
		assertEquals(2, workload.getID());
		assertEquals(50, workload.getSize());
		assertEquals(WorkloadType.RPQ, workload.getType());
		assertEquals(3, workload.getMinConjuncts());
		assertEquals(4, workload.getMaxConjuncts());
		assertEquals(0.5D, workload.getStarProbability());
		assertEquals(0, workload.getMinArity());
		assertEquals(4, workload.getMaxArity());
		assertEquals(3, workload.getMaxSelectivityGraphLength());
		
		Set<QueryShape> shapes = workload.getShapes();
		for(QueryShape shape : QueryShape.values()){
			switch(shape){
			case CHAIN:
			case CYCLE:
				assertTrue(shapes.contains(shape));
				break;
			default:
				assertFalse(shapes.contains(shape));
				break;
			}
		}
		
		Set<Selectivity> sels = workload.getSelectivities();
		for(Selectivity sel : Selectivity.values()){
			if(sel == Selectivity.CONSTANT || sel == Selectivity.QUADRATIC){
				assertTrue(sels.contains(sel));
			}else{
				assertFalse(sels.contains(sel));
			}
		}
	}
	
	@Test
	public void predicateParse(){
		assertEquals(4, config.getSchema().getPredicateCount());
		
		//id
		for(int i = 0; i < config.getPredicates().size(); i++){
			assertEquals(i, config.getPredicates().get(i).getID());
		}
		
		//alias
		assertEquals("authors", config.getPredicates().get(0).getAlias());
		assertEquals("publishedIn", config.getPredicates().get(1).getAlias());
		assertEquals("heldIn", config.getPredicates().get(2).getAlias());
		assertEquals("extendedTo", config.getPredicates().get(3).getAlias());
		
		//TODO test proportion
	}
	
	@Test
	public void typeParse(){
		assertEquals(5, config.getSchema().getTypeCount());
		
		//id
		for(int i = 0; i < config.getPredicates().size(); i++){
			assertEquals(i, config.getTypes().get(i).getID());
		}
		
		//alias
		assertEquals("researcher", config.getTypes().get(0).getAlias());
		assertEquals("paper", config.getTypes().get(1).getAlias());
		assertEquals("journal", config.getTypes().get(2).getAlias());
		assertEquals("conference", config.getTypes().get(3).getAlias());
		assertEquals("city", config.getTypes().get(4).getAlias());
		
		//TODO test proportion/fixed
	}
	
	@Test
	public void edgeParse(){
		//assumes edges are in config read order, this is true currently, but not strictly required
		assertEquals(4, config.getSchema().getEdgeCount());
		
		//first edge
		Edge edge = config.getSchema().getEdges().get(0);
		assertEquals(config.getTypes().get(0), edge.getSourceType());
		assertEquals(config.getTypes().get(1), edge.getTargetType());
		assertEquals(DistributionType.GAUSSIAN, edge.getInDistribution().getType());
		assertEquals(DistributionType.ZIPFIAN, edge.getOutDistribution().getType());
		
		//second edge
		edge = config.getSchema().getEdges().get(1);
		assertEquals(config.getTypes().get(1), edge.getSourceType());
		assertEquals(config.getTypes().get(3), edge.getTargetType());
		assertEquals(DistributionType.GAUSSIAN, edge.getInDistribution().getType());
		assertEquals(DistributionType.UNIFORM, edge.getOutDistribution().getType());

		//third edge
		edge = config.getSchema().getEdges().get(2);
		assertEquals(config.getTypes().get(1), edge.getSourceType());
		assertEquals(config.getTypes().get(2), edge.getTargetType());
		assertEquals(DistributionType.ZIPFIAN, edge.getInDistribution().getType());
		assertEquals(DistributionType.UNIFORM, edge.getOutDistribution().getType());

		//fourth edge
		edge = config.getSchema().getEdges().get(3);
		assertEquals(config.getTypes().get(3), edge.getSourceType());
		assertEquals(config.getTypes().get(4), edge.getTargetType());
		assertEquals(DistributionType.ZIPFIAN, edge.getInDistribution().getType());
		assertEquals(DistributionType.UNIFORM, edge.getOutDistribution().getType());
		
		//TODO test distribution parameters
	}
}
