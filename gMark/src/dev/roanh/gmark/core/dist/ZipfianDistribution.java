package dev.roanh.gmark.core.dist;

import org.w3c.dom.Element;

import dev.roanh.gmark.core.Distribution;
import dev.roanh.gmark.core.DistributionType;

/**
 * Represents a zipfian distribution
 * with some given alpha value.
 * @author Roan
 */
public class ZipfianDistribution implements Distribution{

	/**
	 * Constructs a new zipfian distribution from
	 * the given configuration XML element.
	 * @param data The XML element to parse.
	 */
	public ZipfianDistribution(Element data){
		//TODO
	}
	
	@Override
	public DistributionType getType(){
		return DistributionType.ZIPFIAN;
	}
}
