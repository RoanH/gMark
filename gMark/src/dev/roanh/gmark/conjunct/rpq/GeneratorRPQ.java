package dev.roanh.gmark.conjunct.rpq;

import dev.roanh.gmark.core.ConjunctGenerator;
import dev.roanh.gmark.exception.GenerationException;
import dev.roanh.gmark.query.Conjunct;
import dev.roanh.gmark.query.Variable;
import dev.roanh.gmark.util.SelectivityGraph;
import dev.roanh.gmark.util.SelectivityType;

/**
 * Generator for RPQs (Regular Path Queries).
 * @author Roan
 */
public class GeneratorRPQ implements ConjunctGenerator{
	/**
	 * The workload specifying what RPQs to generate.
	 */
	@SuppressWarnings("unused")//TODO remove
	private WorkloadRPQ workload;
	
	/**
	 * Constructs a new RPQ generator using the given workload.
	 * @param wl The workload specification.
	 * @see WorkloadRPQ
	 */
	public GeneratorRPQ(WorkloadRPQ wl){
		workload = wl;
	}
	
	@Override
	public Conjunct generateConjunct(SelectivityGraph gSel, SelectivityType start, SelectivityType end, Variable source, Variable target, boolean star) throws GenerationException{
		//TODO implement
		throw new GenerationException("Generating RPQ queries is not yet supported.");
	}
}
