package dev.roanh.gmark.conjunct.cpq;

import dev.roanh.gmark.query.Conjunct;
import dev.roanh.gmark.query.Variable;

public class ConjunctCPQ extends Conjunct{
	private CPQ cpq;
	
	public ConjunctCPQ(Variable source, Variable target, boolean star, CPQ cpq){
		super(source, target, star);
		this.cpq = cpq;
	}
}
