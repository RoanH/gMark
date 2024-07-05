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

import java.util.Locale;
import java.util.function.BiFunction;

import org.w3c.dom.Element;

import dev.roanh.gmark.core.graph.Schema;
import dev.roanh.gmark.query.conjunct.cpq.WorkloadCPQ;
import dev.roanh.gmark.query.conjunct.rpq.WorkloadRPQ;

/**
 * Enum specifying all the different concrete workload types.
 * @author Roan
 * @see Workload
 */
public enum WorkloadType{
	/**
	 * Implementation of a workload that uses regular
	 * path queries (RPQ) to fill conjuncts.
	 */
	RPQ("rpq", WorkloadRPQ::new),
	/**
	 * Implementation of a workload that uses conjunctive
	 * path queries (CPQ) to fill conjuncts.
	 */
	CPQ("cpq", WorkloadCPQ::new);
	
	/**
	 * The ID of this workload (as used in configuration files).
	 */
	private String id;
	/**
	 * A function to construct a new workload of this type
	 * from a given configuration file element and graph schema.
	 */
	private BiFunction<Element, Schema, Workload> constructor;

	/**
	 * Constructs a new workload type with the given
	 * ID and given function to construct a new workload
	 * instance from a configuration file element and graph schema.
	 * @param id The workload ID.
	 * @param ctor The workload instance constructor.
	 */
	private WorkloadType(String id, BiFunction<Element, Schema, Workload> ctor){
		this.id = id;
		this.constructor = ctor;
	}
	
	/**
	 * Gets the display name of this workload type.
	 * @return The display name of this workload type.
	 */
	public String getName(){
		return id.toUpperCase(Locale.ROOT);
	}
	
	/**
	 * The ID of this workload type as used in configuration files.
	 * @return The ID of this workload type.
	 */
	public String getID(){
		return id;
	}
	
	/**
	 * Parses a workload configuration from the given
	 * configuration file element and for the given graph schema.
	 * <p>
	 * <b>Note:</b> unrecognised workloads are parsed as {@link #RPQ}
	 * for backwards compatibility reasons with the original version of gMark.
	 * @param elem The configuration file element to parse.
	 * @param schema The graph schema to use.
	 * @return The parsed workload configuration.
	 */
	public static final Workload parse(Element elem, Schema schema){
		for(WorkloadType type : values()){
			if(type.id.equals(elem.getAttribute("type"))){
				return type.constructor.apply(elem, schema);
			}
		}
		
		//backwards compatibility with gMark
		return RPQ.constructor.apply(elem, schema);
	}
}
