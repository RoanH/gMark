/*
 * gMark: A domain- and query language-independent query workload generator and query language utility library.
 * Copyright (C) 2021  Roan Hofland (roan@roanh.dev).  All rights reserved.
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
 * along with gMark.  If not, see <http://www.gnu.org/licenses/>.
 */
package dev.roanh.cpqindex;

import java.util.List;

import dev.roanh.gmark.lang.cpq.CPQ;

/**
 * Common query interface for indexes that support conjunctive path queries.
 * @author Roan
 */
public interface CPQIndex{
	/**
	 * Runs the given query on this index.
	 * @param cpq The query to run.
	 * @return The source-target pairs matched by the query.
	 * @throws IllegalArgumentException When the query is not supported by this index.
	 */
	public abstract List<Pair> query(CPQ cpq) throws IllegalArgumentException;

	/**
	 * Computes the number of source-target pairs matched by the given query.
	 * @param cpq The query to evaluate.
	 * @return The number of matched source-target pairs.
	 * @throws IllegalArgumentException When the query is not supported by this index.
	 */
	public abstract long computeResultCardinality(CPQ cpq) throws IllegalArgumentException;

	/**
	 * Gets the k-path-bisimulation diameter used by this index.
	 * @return The value of k for this index.
	 */
	public abstract int getK();
}
