package dev.roanh.gmark.core;

import java.util.Locale;
import java.util.function.BiFunction;

import org.w3c.dom.Element;

import dev.roanh.gmark.conjunct.cpq.WorkloadCPQ;
import dev.roanh.gmark.conjunct.rpq.WorkloadRPQ;
import dev.roanh.gmark.core.graph.Schema;

/**
 * Enum specifying all the different concrete workload types.
 * @author Roan
 * @see Workload
 */
public enum WorkloadType{
	/**
	 * Implementation of a workload that uses regular
	 * path queries (RPQ) to fill conjuncts.
	 */
	RPQ("rpq", WorkloadRPQ::new),
	/**
	 * Implementation of a workload that uses conjunctive
	 * path queries (CPQ) to fill conjuncts.
	 */
	CPQ("cpq", WorkloadCPQ::new);
	
	/**
	 * The ID of this workload (as used in configuration files).
	 */
	private String id;
	/**
	 * A function to construct a new workload of this type
	 * from a given configuration file element and graph schema.
	 */
	private BiFunction<Element, Schema, Workload> constructor;

	private WorkloadType(String id, BiFunction<Element, Schema, Workload> ctor){
		this.id = id;
		this.constructor = ctor;
	}
	
	/**
	 * Gets the display name of this workload type.
	 * @return The display name of this workload type.
	 */
	public String getName(){
		return id.toUpperCase(Locale.ROOT);
	}
	
	public String getID(){
		return id;
	}
	
	public static final Workload parse(Element elem, Schema schema){
		for(WorkloadType type : values()){
			if(type.id.equals(elem.getAttribute("type"))){
				return type.constructor.apply(elem, schema);
			}
		}
		
		//backwards compatibility with gMark
		return RPQ.constructor.apply(elem, schema);
	}
}
