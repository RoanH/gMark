package dev.roanh.gmark.conjunct.cpq;

import org.w3c.dom.Element;

import dev.roanh.gmark.core.Workload;
import dev.roanh.gmark.core.WorkloadType;

public class WorkloadCPQ extends Workload{
	private int maxDiameter;
	private int maxRecursion;

	public WorkloadCPQ(Element elem){
		super(elem);
		// TODO Auto-generated constructor stub
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
