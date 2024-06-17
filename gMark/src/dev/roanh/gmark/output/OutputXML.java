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
package dev.roanh.gmark.output;

import dev.roanh.gmark.util.IndentWriter;

/**
 * Interface for components that can be converted
 * to an XML representation.
 * @author Roan
 */
public abstract interface OutputXML{

	/**
	 * Writes this object as XML to the given writer.
	 * @param writer The writer to write to.
	 */
	public abstract void writeXML(IndentWriter writer);
	
	/**
	 * Gets the XML form of this object.
	 * @return The XML form of this object.
	 */
	public default String toXML(){
		IndentWriter writer = new IndentWriter();
		writeXML(writer);
		return writer.toString();
	}
}
