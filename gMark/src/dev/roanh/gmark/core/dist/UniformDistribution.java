package dev.roanh.gmark.core.dist;

import org.w3c.dom.Element;

import dev.roanh.gmark.core.Distribution;
import dev.roanh.gmark.core.DistributionType;

/**
 * Represents a uniform distribution where
 * number are uniformly distributed between
 * some given minimum and maximum number.
 * @author Roan
 */
public class UniformDistribution implements Distribution{

	/**
	 * Constructs a new uniform distribution from
	 * the given configuration XML element.
	 * @param data The XML element to parse.
	 */
	public UniformDistribution(Element data){
		//TODO
	}

	@Override
	public DistributionType getType(){
		return DistributionType.UNIFORM;
	}
}
