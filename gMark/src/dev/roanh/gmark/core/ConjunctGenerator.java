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
