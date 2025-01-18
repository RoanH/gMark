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
public class ConfigGraph extends UniqueGraph<Type, Predicate>{

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
