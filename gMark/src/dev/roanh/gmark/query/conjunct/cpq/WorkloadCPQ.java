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
package dev.roanh.gmark.query.conjunct.cpq;

import org.w3c.dom.Element;

import dev.roanh.gmark.ConfigParser;
import dev.roanh.gmark.core.ConjunctGenerator;
import dev.roanh.gmark.core.Workload;
import dev.roanh.gmark.core.WorkloadType;
import dev.roanh.gmark.core.graph.Schema;
import dev.roanh.gmark.lang.cpq.CPQ;

/**
 * Describes a workload of CPQ queries to generate.
 * @author Roan
 * @see CPQ
 */
public class WorkloadCPQ extends Workload{
	/**
	 * The maximum diameter of the CPQs to generate.
	 */
	private int maxDiameter;
	/**
	 * The maximum recursive depth of the CPQs to generate.
	 */
	private int maxRecursion;
	
	/**
	 * Constructs a new CPQ workload from the given
	 * configuration element and graph schema.
	 * @param elem The configuration element to parse.
	 * @param schema The graph schema to use.
	 */
	public WorkloadCPQ(Element elem, Schema schema){
		super(elem, schema);
		Element size = ConfigParser.getElement(elem, "size");
		
		maxDiameter = Integer.parseInt(ConfigParser.getElement(size, "diameter").getAttribute("max"));
		maxRecursion = Integer.parseInt(ConfigParser.getElement(size, "recursion").getAttribute("max"));
	}
	
	/**
	 * Sets the maximum diameter of the CPQs to generate.
	 * @param diameter The new maximum diameter.
	 */
	public void setMaxDiameter(int diameter){
		maxDiameter = diameter;
	}
	
	/**
	 * Sets the maximum recursive depth of the CPQs to generate.
	 * @param recursion The new maximum recursive depth.
	 */
	public void setMaxRecursion(int recursion){
		maxRecursion = recursion;
	}

	/**
	 * Gets the maximum diameter of the CPQs to generate.
	 * @return The maximum diameter.
	 */
	public int getMaxDiameter(){
		return maxDiameter;
	}
	
	/**
	 * Gets the maximum recursive depth of the CPQs to generate.
	 * @return The maximum recursive depth.
	 */
	public int getMaxRecursion(){
		return maxRecursion;
	}
	
	@Override
	public void validate() throws IllegalStateException{
		super.validate();
		
		if(maxDiameter < 1){
			throw new IllegalStateException("Maximum diameter cannot be less than 1.");
		}else if(maxRecursion < 0){
			throw new IllegalStateException("Maximum recursion cannot be negative.");
		}
	}
	
	@Override
	public WorkloadType getType(){
		return WorkloadType.CPQ;
	}

	@Override
	public int getMaxSelectivityGraphLength(){
		return maxDiameter;
	}

	@Override
	public ConjunctGenerator getConjunctGenerator(){
		return new ConjunctGeneratorCPQ(this);
	}
}
