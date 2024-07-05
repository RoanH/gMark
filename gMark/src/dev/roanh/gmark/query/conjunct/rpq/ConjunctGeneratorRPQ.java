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
package dev.roanh.gmark.query.conjunct.rpq;

import dev.roanh.gmark.core.ConjunctGenerator;
import dev.roanh.gmark.exception.GenerationException;
import dev.roanh.gmark.query.Conjunct;
import dev.roanh.gmark.query.Variable;
import dev.roanh.gmark.util.SelectivityGraph;
import dev.roanh.gmark.util.SelectivityType;

/**
 * Conjunct generator for RPQs (Regular Path Queries).
 * @author Roan
 */
public class ConjunctGeneratorRPQ implements ConjunctGenerator{
	/**
	 * The workload specifying what RPQs to generate.
	 */
	@SuppressWarnings("unused")//TODO remove -- also in exclude
	private WorkloadRPQ workload;
	
	/**
	 * Constructs a new RPQ generator using the given workload.
	 * @param wl The workload specification.
	 * @see WorkloadRPQ
	 */
	public ConjunctGeneratorRPQ(WorkloadRPQ wl){
		workload = wl;
	}
	
	@Override
	public Conjunct generateConjunct(SelectivityGraph gSel, SelectivityType start, SelectivityType end, Variable source, Variable target, boolean star) throws GenerationException{
		//TODO implement
		throw new GenerationException("Generating RPQ queries is not yet supported.");
	}
}
