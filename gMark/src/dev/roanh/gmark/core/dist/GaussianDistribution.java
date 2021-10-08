package dev.roanh.gmark.core.dist;

import org.w3c.dom.Element;

import dev.roanh.gmark.core.Distribution;
import dev.roanh.gmark.core.DistributionType;

public class GaussianDistribution implements Distribution{
	
	public GaussianDistribution(Element data){
		//TODO
	}
	
	@Override
	public DistributionType getType(){
		return DistributionType.GAUSSIAN;
	}
}
