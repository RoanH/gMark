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
package dev.roanh.gmark.data;

import dev.roanh.gmark.eval.PathQuery;

/**
 * Cardinality statistics for a reachability path query.
 * @author Roan
 * @param sources The number of distinct possible source nodes for the query, these are all
 *        potential start point nodes for a query. This will be 1 by definition
 *        if a query has a bound source node (unless the query matches nothing).
 * @param paths The total number of distinct paths between the source and target nodes.
 * @param targets The number of distinct possible target nodes for the query, these are all
 *        potential end point nodes for a query. This will be 1 by definition
 *        if a query has a bound target node (unless the query matches nothing).
 * @see PathQuery
 */
public record CardStat(int sources, int paths, int targets){
	
	@Override
	public String toString(){
		return "(sources=%d, paths=%d, targets=%d)".formatted(sources, paths, targets);
	}
}
