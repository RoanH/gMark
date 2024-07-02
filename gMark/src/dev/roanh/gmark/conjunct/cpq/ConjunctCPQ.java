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
package dev.roanh.gmark.conjunct.cpq;

import dev.roanh.gmark.core.WorkloadType;
import dev.roanh.gmark.query.Conjunct;
import dev.roanh.gmark.query.Variable;
import dev.roanh.gmark.util.IndentWriter;

/**
 * Defines a conjunct that has a CPQ as its inner query.
 * @author Roan
 * @see Conjunct
 * @see CPQ
 */
public class ConjunctCPQ extends Conjunct{
	/**
	 * The CPQ in this conjunct.
	 */
	private CPQ cpq;
	
	/**
	 * Constructs a new CPQ conjunct with the given CPQ.
	 * @param cpq The CPQ for this conjunct.
	 * @param source The source variable of the conjunct.
	 * @param target The target variable of the conjunct.
	 * @param star True if the conjunct should have a Kleene star above it.
	 */
	public ConjunctCPQ(CPQ cpq, Variable source, Variable target, boolean star){
		super(source, target, star);
		this.cpq = cpq;
	}
	
	/**
	 * Gets the CPQ for this conjunct.
	 * @return The CPQ for this conjunct.
	 * @see CPQ
	 */
	public CPQ getCPQ(){
		return cpq;
	}

	@Override
	protected String getInnerString(){
		return cpq.toString();
	}

	@Override
	protected void writePartialSQL(IndentWriter writer){
		cpq.writeSQL(writer);
	}

	@Override
	protected void writePartialXML(IndentWriter writer){
		cpq.writeXML(writer);
	}

	@Override
	public WorkloadType getType(){
		return WorkloadType.CPQ;
	}
}
