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

import dev.roanh.gmark.exception.GenerationException;
import dev.roanh.gmark.query.Conjunct;
import dev.roanh.gmark.query.Variable;
import dev.roanh.gmark.util.SelectivityGraph;
import dev.roanh.gmark.util.SelectivityType;

/**
 * Interface for generators that can generate a single
 * random conjuncts of a larger query.
 * @author Roan
 * @see Conjunct
 */
public abstract interface ConjunctGenerator{

	/**
	 * Generates a single conjunct using the given selectivity graph
	 * and with the start of the conjunct being the given start
	 * selectivity type and the end of the conjunct the given end
	 * selectivity type.
	 * @param gSel The selectivity graph to use (if required).
	 * @param start The starting selectivity type of the conjunct to generate.
	 * @param end The end selectivity type of the conjunct to generate.
	 * @param source The source variable of the conjunct.
	 * @param target The target variable of the conjunct.
	 * @param star True if the conjunct should have a Kleene star above it.
	 * @return The randomly generated conjunct.
	 * @throws GenerationException When some exception occurred that
	 *         prevented a conjunct from begin generated.
	 */
	public abstract Conjunct generateConjunct(SelectivityGraph gSel, SelectivityType start, SelectivityType end, Variable source, Variable target, boolean star) throws GenerationException;
}
