package dev.roanh.gmark.core.graph;

import dev.roanh.gmark.core.Distribution;
import dev.roanh.gmark.core.DistributionType;
import dev.roanh.gmark.core.SelectivityClass;

/**
 * Represents a single edge in the graph schema. A
 * graph schema edge also store information about
 * the overall presence of all its edges in a concrete
 * graph instance. This is done using degrees distributions
 * on the in and out degree of the nodes the edges
 * are connected to.
 * @author Roan
 * @see Schema
 * @see Type
 * @see Predicate
 */
public class Edge{
	/**
	 * The type of the source node for this edge.
	 */
	private Type source;
	/**
	 * The type of the target node for this edge.
	 */
	private Type target;
	/**
	 * The predicate, symbol or label on this edge.
	 */
	private Predicate symbol;
	/**
	 * The in distribution of nodes this edge starts from.
	 * This distribution models the degree of nodes concrete
	 * edge instances start from in a graph.
	 */
	private Distribution inDistribution;
	/**
	 * The out distribution of nodes this edge ends at.
	 * This distribution models the degree of nodes concrete
	 * edge instances end at in a graph.
	 */
	private Distribution outDistribution;
	
	/**
	 * Constructs a new edge with the given source type,
	 * target type, predicate, in degree distribution and
	 * out degree distribution.
	 * @param source The source type.
	 * @param target The target type.
	 * @param symbol The edge predicate.
	 * @param in The edge in distribution.
	 * @param out The edge out distribution.
	 */
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
