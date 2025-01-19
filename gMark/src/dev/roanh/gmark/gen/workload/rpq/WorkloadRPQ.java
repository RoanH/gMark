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
package dev.roanh.gmark.gen.workload.rpq;

import org.w3c.dom.Element;

import dev.roanh.gmark.gen.workload.ConfigParser;
import dev.roanh.gmark.gen.workload.ConjunctGenerator;
import dev.roanh.gmark.gen.workload.Workload;
import dev.roanh.gmark.gen.workload.WorkloadType;
import dev.roanh.gmark.type.schema.Schema;

/**
 * Describes a workload of RPQ queries to generate.
 * @author Roan
 */
public class WorkloadRPQ extends Workload{
	/**
	 * The minimum number of disjuncts in the RPQs to generate.
	 */
	private int minDisjuncts;
	/**
	 * The maximum number of disjuncts in the RPQs to generate.
	 */
	private int maxDisjuncts;
	/**
	 * The minimum length of the RPQs to generate.
	 */
	private int minLength;
	/**
	 * The maximum length of hte RPQs to generate.
	 */
	private int maxLength;
	
	/**
	 * Constructs a new RPQ workload from the given
	 * configuration element and graph schema.
	 * @param elem The configuration element to parse.
	 * @param schema The graph schema to use.
	 */
	public WorkloadRPQ(Element elem, Schema schema){
		super(elem, schema);
		Element size = ConfigParser.getElement(elem, "size");
		
		Element disj = ConfigParser.getElement(size, "disjuncts");
		minDisjuncts = Integer.parseInt(disj.getAttribute("min"));
		maxDisjuncts = Integer.parseInt(disj.getAttribute("max"));
		
		Element len = ConfigParser.getElement(size, "length");
		minLength = Integer.parseInt(len.getAttribute("min"));
		maxLength = Integer.parseInt(len.getAttribute("max"));
	}
	
	/**
	 * Sets the maximum length of the RPQs to generate.
	 * @param len The new maximum length.
	 */
	public void setMaxLength(int len){
		maxLength = len;
	}
	
	/**
	 * Sets the minimum length of the RPQs to generate.
	 * @param len The new minimum length.
	 */
	public void setMinLength(int len){
		minLength = len;
	}
	
	/**
	 * Sets the maximum number of disjuncts in the RPQs to generate.
	 * @param disj The new maximum disjunct count.
	 */
	public void setMaxDisjuncts(int disj){
		maxDisjuncts = disj;
	}
	
	/**
	 * Sets the minimum number of disjuncts in the RPQs to generate.
	 * @param disj The new minimum disjunct count.
	 */
	public void setMinDisjuncts(int disj){
		minDisjuncts = disj;
	}
	
	/**
	 * Gets the maximum length of the RPQs to generate.
	 * @return The maximum RPQ length.
	 */
	public int getMaxLength(){
		return maxLength;
	}
	
	/**
	 * Gets the minimum length of the RPQs to generate.
	 * @return The minimum RPQ length.
	 */
	public int getMinLength(){
		return minLength;
	}
	
	/**
	 * Gets the minimum number of disjuncts in the RPQs to generate.
	 * @return The minimum number of disjuncts.
	 */
	public int getMinDisjuncts(){
		return minDisjuncts;
	}
	
	/**
	 * Gets the maximum number of disjuncts in the RPQs to generate.
	 * @return The maximum number of disjuncts.
	 */
	public int getMaxDisjuncts(){
		return maxDisjuncts;
	}
	
	@Override
	public void validate() throws IllegalStateException{
		super.validate();
		
		if(minDisjuncts < 1){
			throw new IllegalStateException("Minimum number of disjuncts cannot be less than 1.");
		}else if(minDisjuncts > maxDisjuncts){
			throw new IllegalStateException("Minimum number of disjuncts cannot be more than the maximum number of disjuncts.");
		}else if(minLength < 1){
			throw new IllegalStateException("Minimum length cannot be less than 1.");
		}else if(minLength > maxLength){
			throw new IllegalStateException("Minimum length cannot be greater than the maximum length.");
		}
		
		//TODO remove after implementation
		throw new IllegalStateException("RPQ workload generation is not yet supported.");
	}

	@Override
	public WorkloadType getType(){
		return WorkloadType.RPQ;
	}

	@Override
	public int getMaxSelectivityGraphLength(){
		return maxLength;
	}

	@Override
	public ConjunctGenerator getConjunctGenerator(){
		return new ConjunctGeneratorRPQ(this);
	}
}
