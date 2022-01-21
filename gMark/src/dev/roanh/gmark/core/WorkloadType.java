package dev.roanh.gmark.core;

import java.util.Locale;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.w3c.dom.Element;

import dev.roanh.gmark.conjunct.cpq.GeneratorCPQ;
import dev.roanh.gmark.conjunct.cpq.WorkloadCPQ;
import dev.roanh.gmark.conjunct.rpq.GeneratorRPQ;
import dev.roanh.gmark.conjunct.rpq.WorkloadRPQ;
import dev.roanh.gmark.core.graph.Schema;

public enum WorkloadType{
	RPQ("rpq", WorkloadRPQ::new, GeneratorRPQ::new),
	CPQ("cpq", WorkloadCPQ::new, GeneratorCPQ::new);
	
	private String name;
	private BiFunction<Element, Schema, Workload> constructor;
	private Function<Workload, ConjunctGenerator> conjGen;

	private WorkloadType(String name, BiFunction<Element, Schema, Workload> ctor, Function<Workload, ConjunctGenerator> conjGen){
		this.name = name;
		this.constructor = ctor;
		this.conjGen = conjGen;
	}
	
	public String getName(){
		return name.toUpperCase(Locale.ROOT);
	}
	
	public ConjunctGenerator getConjunctGenerator(Workload workload){
		return conjGen.apply(workload);
	}
	
	public static Workload parse(Element elem, Schema schema){
		for(WorkloadType type : values()){
			if(type.name.equals(elem.getAttribute("type"))){
				return type.constructor.apply(elem, schema);
			}
		}
		
		//backwards compatibility with gMark
		return RPQ.constructor.apply(elem, schema);
	}
}
