package dev.roanh.gmark.conjunct.rpq;

import org.w3c.dom.Element;

import dev.roanh.gmark.core.Workload;
import dev.roanh.gmark.core.WorkloadType;

/**
 * Describes a workload of RPQ queries to generate.
 * @author Roan
 */
public class WorkloadRPQ extends Workload{
	/**
	 * Probability that a conjunct has a Kleene star above.
	 */
	private double multiplicity;
	private int minDisjuncts;
	private int maxDisjuncts;
	private int minLength;
	private int maxLength;
	
	public WorkloadRPQ(Element elem){
		super(elem);
		// TODO Auto-generated constructor stub
	}
	
	public int getMaxLength(){
		return maxLength;
	}
	
	public int getMinLength(){
		return minLength;
	}

	@Override
	public WorkloadType getType(){
		return WorkloadType.RPQ;
	}

	@Override
	public int getMaxSelectivityGraphLength(){
		return maxLength;
	}
}
