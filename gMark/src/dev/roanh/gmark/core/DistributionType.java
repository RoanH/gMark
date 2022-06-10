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
package dev.roanh.gmark.core;

import java.util.function.Function;

import org.w3c.dom.Element;

import dev.roanh.gmark.core.dist.GaussianDistribution;
import dev.roanh.gmark.core.dist.UniformDistribution;
import dev.roanh.gmark.core.dist.ZipfianDistribution;

/**
 * Enum of all probability distributions supported by gMark.
 * @author Roan
 */
public enum DistributionType{
	/**
	 * Indicates that the distribution is not known.
	 */
	UNDEFINED("undefined", e->Distribution.UNDEFINED),
	/**
	 * Represents a uniform distribution where
	 * number are uniformly distributed between
	 * some given minimum and maximum number.
	 */
	UNIFORM("uniform", UniformDistribution::new),
	/**
	 * Represents a gaussian (normal) distribution
	 * where numbers are normally distributed with
	 * some given mean and standard deviation.
	 */
	GAUSSIAN("gaussian", GaussianDistribution::new),
	/**
	 * Represents a zipfian distribution
	 * with some given alpha value.
	 */
	ZIPFIAN("zipfian", ZipfianDistribution::new);
	
	/**
	 * The ID of this distribution (as used in config files).
	 */
	private String id;
	/**
	 * Function to construct a distribution from a configuration element.
	 */
	private Function<Element, Distribution> constructor;

	/**
	 * Constructs a new distribution type with the given ID and constructor.
	 * @param id The configuration name of this distribution.
	 * @param ctor A function to construct this distribution from
	 *        a configuration file element.
	 */
	private DistributionType(String id, Function<Element, Distribution> ctor){
		this.id = id;
		this.constructor = ctor;
	}
	
	/**
	 * Gets the id of this distribution (as used in configuration files).
	 * @return The name of this distribution.
	 */
	public String getID(){
		return id;
	}
	
	/**
	 * Constructs a new instance of this distribution from the given
	 * configuration file element.
	 * @param data The configuration file element to parse.
	 * @return The newly constructed distribution.
	 */
	public Distribution newInstance(Element data){
		return constructor.apply(data);
	}
	
	@Override
	public String toString(){
		return id;
	}
	
	/**
	 * Resolves a distribution type by its configuration file ID.
	 * @param id The configuration type ID to search for.
	 * @return The distribution with the given configuration ID
	 *         or <code>null</code> if no distribution type with
	 *         the given ID was found.
	 */
	public static final DistributionType getByName(String id){
		for(DistributionType type : values()){
			if(type.id.equalsIgnoreCase(id)){
				return type;
			}
		}
		return null;
	}
}
