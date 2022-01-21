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

	public int getMaxDiameter(){
		return maxDiameter;
	}
	
	public int getMaxRecursion(){
		return maxRecursion;
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
