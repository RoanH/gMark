package dev.roanh.gmark.conjunct.rpq;

import org.w3c.dom.Element;

import dev.roanh.gmark.ConfigParser;
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
	public WorkloadType getType(){
		return WorkloadType.RPQ;
	}

	@Override
	public int getMaxSelectivityGraphLength(){
		return maxLength;
	}
}
