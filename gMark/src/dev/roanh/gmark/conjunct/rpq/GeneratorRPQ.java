package dev.roanh.gmark.conjunct.rpq;

import dev.roanh.gmark.core.ConjunctGenerator;
import dev.roanh.gmark.core.Workload;
import dev.roanh.gmark.exception.GenerationException;
import dev.roanh.gmark.query.Conjunct;
import dev.roanh.gmark.util.SelectivityGraph;
import dev.roanh.gmark.util.SelectivityType;

public class GeneratorRPQ implements ConjunctGenerator{
	private WorkloadRPQ workload;
	
	public GeneratorRPQ(Workload wl){
		workload = (WorkloadRPQ)wl;
	}
	
	@Override
	public Conjunct generateConjunct(SelectivityGraph gSel, SelectivityType start, SelectivityType end) throws GenerationException{
		// TODO Auto-generated method stub
		return null;
	}
}
