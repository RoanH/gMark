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
import dev.roanh.gmark.util.Graph.GraphEdge;
import dev.roanh.gmark.util.Graph.GraphNode;

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
