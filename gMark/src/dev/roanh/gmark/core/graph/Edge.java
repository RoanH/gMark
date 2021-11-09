package dev.roanh.gmark.core.graph;

import dev.roanh.gmark.core.Distribution;
import dev.roanh.gmark.core.DistributionType;
import dev.roanh.gmark.core.SelectivityClass;

public class Edge{
	private Type source;
	private Type target;
	private Predicate symbol;//edge predicate
	//TODO there is an edge_type -- use currently unknown
	private Distribution inDistribution;
	private Distribution outDistribution;
	
	public Edge(Type source, Type target, Predicate symbol, Distribution in, Distribution out){
		this.source = source;
		this.target = target;
		this.symbol = symbol;
		inDistribution = in;
		outDistribution = out;
	}
	
	public Distribution getInDistribution(){
		return inDistribution;
	}
	
	public Distribution getOutDistribution(){
		return outDistribution;
	}
	
	//subject
	public Type getSourceType(){
		return source;
	}
	
	//object
	public Type getTargetType(){
		return target;
	}
	
	//symbol
	public Predicate getPredicate(){
		return symbol;
	}

	public SelectivityClass getSelectivty(){
		if(!source.isScalable() && !target.isScalable()){
			return SelectivityClass.ONE_ONE;
		}else if(source.isScalable() && !target.isScalable()){
			return SelectivityClass.N_ONE;
		}else if(!source.isScalable() && target.isScalable()){
			return SelectivityClass.ONE_N;
		}else if(outDistribution.getType() == DistributionType.ZIPFIAN && inDistribution.getType() == DistributionType.ZIPFIAN){
			return SelectivityClass.LESS_GREATER;
		}else if(outDistribution.getType() == DistributionType.ZIPFIAN && inDistribution.getType() != DistributionType.ZIPFIAN){
			return SelectivityClass.LESS;
		}else if(outDistribution.getType() != DistributionType.ZIPFIAN && inDistribution.getType() == DistributionType.ZIPFIAN){
			return SelectivityClass.GREATER;
		}else{
			return SelectivityClass.EQUALS;
		}
	}
	
	@Override
	public String toString(){
		return "Edge[source=" + source.getID() + ",target=" + target.getID() + ",symbol=" + symbol.getID() + ",in=" + inDistribution.getType() + ",out=" + outDistribution.getType() + "]";
	}
}
