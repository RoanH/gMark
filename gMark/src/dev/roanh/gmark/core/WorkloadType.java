package dev.roanh.gmark.core;

import java.util.function.Function;

import org.w3c.dom.Element;

import dev.roanh.gmark.conjunct.cpq.WorkloadCPQ;
import dev.roanh.gmark.conjunct.rpq.WorkloadRPQ;

public enum WorkloadType{
	RPQ("rpq", WorkloadRPQ::new),
	CPQ("cpq", WorkloadCPQ::new);
	
	private String name;
	private Function<Element, Workload> constructor;

	private WorkloadType(String name, Function<Element, Workload> ctor){
		this.name = name;
		this.constructor = ctor;
	}
	
	public static Workload parse(Element elem){
		for(WorkloadType type : values()){
			if(type.name.equals(elem.getAttribute("type"))){
				return type.constructor.apply(elem);
			}
		}
		
		//backwards compatibility with gMark
		return RPQ.constructor.apply(elem);
	}
}
