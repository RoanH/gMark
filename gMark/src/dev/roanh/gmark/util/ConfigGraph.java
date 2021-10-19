package dev.roanh.gmark.util;

import dev.roanh.gmark.core.graph.Configuration;
import dev.roanh.gmark.core.graph.Predicate;
import dev.roanh.gmark.core.graph.Schema;
import dev.roanh.gmark.core.graph.Type;

public class ConfigGraph extends Graph<Type, Predicate>{

	public ConfigGraph(Configuration config){
		this(config.getSchema());
	}
	
	public ConfigGraph(Schema schema){
		schema.getTypes().forEach(this::addUniqueNode);
		schema.getEdges().forEach(e->addUniqueEdge(e.getSourceType(), e.getTargetType(), e.getPredicate()));
	}
}
