package dev.roanh.gmark;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import dev.roanh.gmark.core.DistributionType;
import dev.roanh.gmark.core.graph.Configuration;
import dev.roanh.gmark.core.graph.Edge;

public class ConfigParserTest{
	private static Configuration config;

	@BeforeAll
	public static void parseConfig(){
		config = ConfigParser.parse(ClassLoader.getSystemResourceAsStream("test.xml"));
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