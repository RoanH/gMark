package dev.roanh.gmark.conjunct.cpq;

import dev.roanh.gmark.core.WorkloadType;
import dev.roanh.gmark.query.Conjunct;
import dev.roanh.gmark.query.Variable;
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
	 * @param source The source variable of the conjunct.
	 * @param target The target variable of the conjunct.
	 * @param star True if the conjunct should have a Kleene star above it.
	 */
	public ConjunctCPQ(CPQ cpq, Variable source, Variable target, boolean star){
		super(source, target, star);
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
