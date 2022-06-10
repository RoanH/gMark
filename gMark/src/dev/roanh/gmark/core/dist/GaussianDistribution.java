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
package dev.roanh.gmark.core.dist;

import org.w3c.dom.Element;

import dev.roanh.gmark.core.Distribution;
import dev.roanh.gmark.core.DistributionType;

/**
 * Represents a gaussian (normal) distribution
 * where numbers are normally distributed with
 * some given mean and standard deviation.
 * @author Roan
 */
public class GaussianDistribution implements Distribution{
	
	/**
	 * Constructs a new gaussian distribution from
	 * the given configuration XML element.
	 * @param data The XML element to parse.
	 */
	public GaussianDistribution(Element data){
		//TODO
	}
	
	@Override
	public DistributionType getType(){
		return DistributionType.GAUSSIAN;
	}
}
