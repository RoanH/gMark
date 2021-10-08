package dev.roanh.gmark.core.dist;

import org.w3c.dom.Element;

import dev.roanh.gmark.core.Distribution;
import dev.roanh.gmark.core.DistributionType;

public class UniformDistribution implements Distribution{
	
	public UniformDistribution(Element data){
		//TODO
	}

	@Override
	public DistributionType getType(){
		return DistributionType.UNIFORM;
	}
}
