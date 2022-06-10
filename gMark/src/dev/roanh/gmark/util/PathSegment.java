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
package dev.roanh.gmark.util;

import dev.roanh.gmark.core.SelectivityClass;
import dev.roanh.gmark.core.graph.Schema;

/**
 * Path segment modelling a single edge of
 * a path drawn in the selectivity graph.
 * In the original gMark codebase a path
 * segment is a called a tripple.
 * @author Roan
 * @see SelectivityGraph
 */
public class PathSegment{
	/**
	 * The starting selectivity type of this path segment.
	 */
	private SelectivityType start;
	/**
	 * The ending selectivity type of this path segment.
	 */
	private SelectivityType end;
	/**
	 * Whether this path segment should be modelled with
	 * a Kleene star above it when generating a query.
	 */
	private boolean star;
	
	/**
	 * Constructs a new path segment with the given start and
	 * end selectivity type and Kleene star status.
	 * @param start The starting selectivity type.
	 * @param end The ending selectivity type.
	 * @param star True if this path segment can handle a Kleene star.
	 */
	public PathSegment(SelectivityType start, SelectivityType end, boolean star){
		this.start = start;
		this.end = end;
		this.star = star;
	}
	
	/**
	 * Gets the source selectivity type of this path
	 * segment. This corresponds to a node from the
	 * selectivity graph the path was drawn from.
	 * @return The source selectivity type.
	 */
	public SelectivityType getSource(){
		return start;
	}
	
	/**
	 * Gets the target selectivity type of this path
	 * segment. This corresponds to a node from the
	 * selectivity graph the path was drawn from.
	 * @return The target selectivity type.
	 */
	public SelectivityType getTarget(){
		return end;
	}
	
	/**
	 * Checks whether this path segment should be modelled with
	 * a Kleene star above it when generating a query.
	 * @return True if this path segment should have a Kleene
	 *         star above it in a query.
	 */
	public boolean hasStar(){
		return star;
	}
	
	/**
	 * Constructs a new path segment with the given start and
	 * end nodes and Kleene star status. The given start and
	 * end node IDs will be resolve to types using the given
	 * schema. The given selectivity class denotes the selectivity
	 * of the target node. The selectivity of the start node will
	 * always be {@link SelectivityClass#EQUALS}.
	 * @param schema The schema to use to resolve types by ID.
	 * @param start The type ID of the start node of the path segment.
	 * @param sel The selectivity of the end node.
	 * @param end The type ID of the end node of the path segment.
	 * @param star True if this path segment can handle a Kleene star.
	 * @return The newly constructed path segment.
	 */
	public static final PathSegment of(Schema schema, int start, SelectivityClass sel, int end, boolean star){
		return new PathSegment(
			SelectivityType.of(schema.getType(start), SelectivityClass.EQUALS),//TODO this is always equals per generate_random_path_aux2 should look into this
			SelectivityType.of(schema.getType(end), sel),
			star
		);
	}
	
	@Override
	public String toString(){
		return "PathSegment[start=" + start + ",end=" + end + ",star=" + star + "]";
	}
}
