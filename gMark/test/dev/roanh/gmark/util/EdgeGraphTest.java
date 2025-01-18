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
package dev.roanh.gmark.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import dev.roanh.gmark.ConfigParser;
import dev.roanh.gmark.core.Configuration;
import dev.roanh.gmark.core.SelectivityClass;
import dev.roanh.gmark.exception.ConfigException;
import dev.roanh.gmark.exception.GenerationException;
import dev.roanh.gmark.util.UniqueGraph.GraphEdge;
import dev.roanh.gmark.util.UniqueGraph.GraphNode;

public class EdgeGraphTest{
	private static final int MAX_LENGTH = 4;
	private static Configuration config;
	private static SelectivityType source;
	private static SelectivityType target;
	private static SchemaGraph gs;
	private static EdgeGraph graph;
	
	@BeforeAll
	public static void constructGraph() throws GenerationException{
		try{
			config = ConfigParser.parse(ClassLoader.getSystemResourceAsStream("test.xml"));
			source = SelectivityType.of(config.getTypes().get(2), SelectivityClass.EQUALS);
			target = SelectivityType.of(config.getTypes().get(1), SelectivityClass.CROSS);
			gs = new SchemaGraph(config.getSchema());
			graph = new EdgeGraph(gs, MAX_LENGTH, source, target);
		}catch(ConfigException e){
			e.printStackTrace();
		}
	}
	
	@Test
	public void correctMaxLength(){
		assertEquals(MAX_LENGTH, graph.getMaxLength());
	}
	
	@Test
	public void pathNotEmpty(){
		assertFalse(graph.drawPath().isEmpty());
	}
	
	@Test
	public void pathConnectedToSource(){
		assertTrue(graph.drawPath().get(0).getInEdges().stream().map(GraphEdge::getSourceNode).anyMatch(graph.getSource()::equals));
	}
	
	@Test
	public void pathConnectedToTarget(){
		List<GraphNode<EdgeGraphData, Void>> path = graph.drawPath();
		assertTrue(path.get(path.size() - 1).getOutEdges().stream().map(GraphEdge::getTargetNode).anyMatch(graph.getTarget()::equals));
	}
	
	@Test
	public void pathMaxLength(){
		assertTrue(graph.drawPath().stream().map(GraphNode::getData).mapToInt(EdgeGraphData::size).sum() <= MAX_LENGTH);
	}
	
	@Test
	public void pathMinLength(){
		for(int i = 1; i <= MAX_LENGTH / 2; i++){
			assertTrue(graph.drawPath(i).stream().map(GraphNode::getData).mapToInt(EdgeGraphData::size).sum() >= i);
		}
	}
}
