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
package dev.roanh.gmark.lang.rpq;

import dev.roanh.gmark.core.graph.Predicate;
import dev.roanh.gmark.lang.generic.GenericEdge;

/**
 * RPQ modelling a single label traversal.
 * @author Roan
 */
public class EdgeRPQ extends GenericEdge implements RPQ{

	/**
	 * Constructs a new edge RPQ with the
	 * given label to traverse.
	 * @param symbol The label to traverse.
	 */
	protected EdgeRPQ(Predicate symbol){
		super(symbol);
	}
}
