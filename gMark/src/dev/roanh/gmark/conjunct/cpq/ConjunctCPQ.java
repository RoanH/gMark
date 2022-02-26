package dev.roanh.gmark.conjunct.cpq;

import dev.roanh.gmark.core.WorkloadType;
import dev.roanh.gmark.query.Conjunct;
import dev.roanh.gmark.util.IndentWriter;

public class ConjunctCPQ extends Conjunct{
	private CPQ cpq;
	
	public ConjunctCPQ(CPQ cpq){
		this.cpq = cpq;
	}

	@Override
	protected String getInnerString(){
		return cpq.toString();
	}

	@Override
	protected String toPartialSQL(){
		return cpq.toSQL();
	}

	@Override
	protected void writePartialXML(IndentWriter writer){
		cpq.
	}

	@Override
	public WorkloadType getType(){
		return WorkloadType.CPQ;
	}
}
