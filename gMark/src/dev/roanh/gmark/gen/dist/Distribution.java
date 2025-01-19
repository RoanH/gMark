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
package dev.roanh.gmark.gen.dist;

import org.w3c.dom.Element;

/**
 * Abstract interface for value distributions.
 * @author Roan
 */
public abstract interface Distribution{
	/**
	 * Used when a distribution is not explicitly specified.
	 */
	public static final Distribution UNDEFINED = ()->DistributionType.UNDEFINED;

	/**
	 * Gets the type of this distribution.
	 * @return The type of this distribution.
	 */
	public abstract DistributionType getType();
	
	/**
	 * Parses the given configuration XML element
	 * to the corresponding distribution.
	 * @param data The XML element to parse.
	 * @return The parsed distribution.
	 */
	public static Distribution fromXML(Element data){
		return DistributionType.getByName(data.getAttribute("type")).newInstance(data);
	}
}
