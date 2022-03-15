package dev.roanh.gmark.conjunct.rpq;

import org.w3c.dom.Element;

import dev.roanh.gmark.ConfigParser;
import dev.roanh.gmark.core.ConjunctGenerator;
import dev.roanh.gmark.core.Workload;
import dev.roanh.gmark.core.WorkloadType;
import dev.roanh.gmark.core.graph.Schema;

/**
 * Describes a workload of RPQ queries to generate.
 * @author Roan
 */
public class WorkloadRPQ extends Workload{
	private int minDisjuncts;
	private int maxDisjuncts;
	private int minLength;
	private int maxLength;
	
	public WorkloadRPQ(Element elem, Schema schema){
		super(elem, schema);
		Element size = ConfigParser.getElement(elem, "size");
		
		Element disj = ConfigParser.getElement(size, "disjuncts");
		minDisjuncts = Integer.parseInt(disj.getAttribute("min"));
		maxDisjuncts = Integer.parseInt(disj.getAttribute("max"));
		
		Element len = ConfigParser.getElement(size, "length");
		minLength = Integer.parseInt(len.getAttribute("min"));
		maxLength = Integer.parseInt(len.getAttribute("max"));
	}
	
	public void setMaxLength(int len){
		maxLength = len;
	}
	
	public void setMinLength(int len){
		minLength = len;
	}
	
	public void setMaxDisjuncts(int disj){
		maxDisjuncts = disj;
	}
	
	public void setMinDisjuncts(int disj){
		minDisjuncts = disj;
	}
	
	public int getMaxLength(){
		return maxLength;
	}
	
	public int getMinLength(){
		return minLength;
	}
	
	public int getMinDisjuncts(){
		return minDisjuncts;
	}
	
	public int getMaxDisjuncts(){
		return maxDisjuncts;
	}
	
	@Override
	public void validate() throws IllegalStateException{
		super.validate();
		
		if(minDisjuncts < 1){
			throw new IllegalStateException("Minimum number of disjuncts cannot be less than 1.");
		}else if(minDisjuncts > maxDisjuncts){
			throw new IllegalStateException("Minimum number of disjuncts cannot be more than the maximum number of disjuncts.");
		}else if(minLength < 1){
			throw new IllegalStateException("Minimum length cannot be less than 1.");
		}else if(minLength > maxLength){
			throw new IllegalStateException("Minimum length cannot be greater than the maximum length.");
		}
	}

	@Override
	public WorkloadType getType(){
		return WorkloadType.RPQ;
	}

	@Override
	public int getMaxSelectivityGraphLength(){
		return maxLength;
	}

	@Override
	public ConjunctGenerator getConjunctGenerator(){
		return new GeneratorRPQ(this);
	}
}
