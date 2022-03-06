package dev.roanh.gmark.conjunct.cpq;

import dev.roanh.gmark.core.WorkloadType;
import dev.roanh.gmark.query.Conjunct;
import dev.roanh.gmark.util.IndentWriter;

/**
 * Defines a conjunct that has a CPQ as
 * its inner query.
 * @author Roan
 * @see Conjunct
 */
public class ConjunctCPQ extends Conjunct{
	/**
	 * The CPQ in this conjunct.
	 */
	private CPQ cpq;
	
	/**
	 * Constructs a new CPQ conjunct with the given CPQ.
	 * @param cpq The CPQ for this conjunct.
	 */
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
		cpq.writeXML(writer);
	}

	@Override
	public WorkloadType getType(){
		return WorkloadType.CPQ;
	}
}
