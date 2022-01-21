package dev.roanh.gmark.util;

import dev.roanh.gmark.core.Configuration;
import dev.roanh.gmark.core.graph.Predicate;
import dev.roanh.gmark.core.graph.Schema;
import dev.roanh.gmark.core.graph.Type;

/**
 * Simple graph that models the gMark graph configuration.
 * This graph has a node for each type and an edge between
 * types that have a configuration for how edges between
 * the two types should look.
 * @author Roan
 * @see Configuration
 * @see Schema
 */
public class ConfigGraph extends Graph<Type, Predicate>{

	/**
	 * Constructs a new configuration graph from
	 * the given gMark configuration.
	 * @param config The gMark configuration.
	 */
	public ConfigGraph(Configuration config){
		this(config.getSchema());
	}
	
	/**
	 * Constructs a new configuration graph
	 * the given graph schema.
	 * @param schema The graph schema.
	 */
	public ConfigGraph(Schema schema){
		schema.getTypes().forEach(this::addUniqueNode);
		schema.getEdges().forEach(e->addUniqueEdge(e.getSourceType(), e.getTargetType(), e.getPredicate()));
	}
}
