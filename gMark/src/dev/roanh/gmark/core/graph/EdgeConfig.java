package dev.roanh.gmark.core.graph;

import dev.roanh.gmark.core.Distribution;
import dev.roanh.gmark.core.DistributionType;
import dev.roanh.gmark.core.SelectivityClass;

public class EdgeConfig{
	private int id;//also known as symbol
	private Distribution inDistribution;
	private Distribution outDistribution;
	private NodeConfig source;
	private NodeConfig target;
	
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
}
