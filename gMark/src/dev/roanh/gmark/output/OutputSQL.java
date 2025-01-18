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
package dev.roanh.gmark.output;

import dev.roanh.gmark.util.IndentWriter;

/**
 * Interface for components that can be converted
 * to an SQL query representing them.
 * @author Roan
 */
public abstract interface OutputSQL{
	
	/**
	 * Writes the SQL representation of this object to the given writer.
	 * @param writer The writer to write to.
	 */
	public abstract void writeSQL(IndentWriter writer);

	/**
	 * Converts this object to an SQL query.
	 * @return An SQL query representing this object.
	 */
	public default String toSQL(){
		IndentWriter writer = new IndentWriter();
		writeSQL(writer);
		return writer.toString();
	}
}
