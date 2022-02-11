package dev.roanh.gmark.conjunct.cpq;

import org.w3c.dom.Element;

import dev.roanh.gmark.ConfigParser;
import dev.roanh.gmark.core.Workload;
import dev.roanh.gmark.core.WorkloadType;
import dev.roanh.gmark.core.graph.Schema;

public class WorkloadCPQ extends Workload{
	private int maxDiameter;
	private int maxRecursion;

	public WorkloadCPQ(Element elem, Schema schema){
		super(elem, schema);
		Element size = ConfigParser.getElement(elem, "size");
		
		maxDiameter = Integer.parseInt(ConfigParser.getElement(size, "diameter").getAttribute("max"));
		maxRecursion = Integer.parseInt(ConfigParser.getElement(size, "recursion").getAttribute("max"));
	}
	
	public void setMaxDiameter(int diameter){
		maxDiameter = diameter;
	}
	
	public void setMaxRecursion(int recursion){
		maxRecursion = recursion;
	}

	public int getMaxDiameter(){
		return maxDiameter;
	}
	
	public int getMaxRecursion(){
		return maxRecursion;
	}
	
	@Override
	public void validate() throws IllegalStateException{
		super.validate();
		
		if(maxDiameter < 1){
			throw new IllegalStateException("Maximum diamter cannot be less than 1.");
		}else if(maxRecursion < 0){
			throw new IllegalStateException("Maximum recursion cannot be negative.");
		}
	}
	
	@Override
	public WorkloadType getType(){
		return WorkloadType.CPQ;
	}

	@Override
	public int getMaxSelectivityGraphLength(){
		return maxDiameter;
	}
}
