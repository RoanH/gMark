package dev.roanh.gmark.core.dist;

import org.w3c.dom.Element;

import dev.roanh.gmark.core.Distribution;
import dev.roanh.gmark.core.DistributionType;

public class ZipfianDistribution implements Distribution{

	public ZipfianDistribution(Element data){
		//TODO
	}
	
	@Override
	public DistributionType getType(){
		return DistributionType.ZIPFIAN;
	}
}
