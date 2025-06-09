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
package dev.roanh.gmark.ast;

/**
 * Representation of a variable in a query definition.
 * @author Roan
 */
public abstract interface QueryVariable{
	
	/**
	 * Gets the display name of this variable.
	 * @return The display name of this variable.
	 */
	public abstract String getName();
	
	/**
	 * Gets whether this is a free variable in the query.
	 * Free variables appear in the output (head) of the query
	 * and will be projected in the query result.
	 * @return True if this is a free variable.
	 * @see #isBound()
	 */
	public abstract boolean isFree();

	/**
	 * Gets whether this is a bound variable in the query.
	 * Bounds variables only appear internally in the body of a query
	 * and are not projected in the query result.
	 * @return True if this is a bound variable.
	 * @see #isFree()
	 */
	public default boolean isBound(){
		return !isFree();
	}
}
