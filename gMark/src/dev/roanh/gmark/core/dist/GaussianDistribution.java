package dev.roanh.gmark.core.dist;

import org.w3c.dom.Element;

import dev.roanh.gmark.core.Distribution;
import dev.roanh.gmark.core.DistributionType;

/**
 * Represents a gaussian (normal) distribution
 * where number of normally distributed with
 * some given mean and standard deviation.
 * @author Roan
 */
public class GaussianDistribution implements Distribution{
	
	/**
	 * Constructs a new gaussian distribution from
	 * the given configuration XML element.
	 * @param data The XML element to parse.
	 */
	public GaussianDistribution(Element data){
		//TODO
	}
	
	@Override
	public DistributionType getType(){
		return DistributionType.GAUSSIAN;
	}
}
