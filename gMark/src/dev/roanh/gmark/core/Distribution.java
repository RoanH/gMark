package dev.roanh.gmark.core;

import org.w3c.dom.Element;

/**
 * Abstract interface for value distributions.
 * @author Roan
 */
public abstract interface Distribution{
	/**
	 * Used when a distribution is not explicitly specified.
	 */
	public static final Distribution UNDEFINED = ()->DistributionType.UNDEFINED;

	/**
	 * Gets the type of this distribution.
	 * @return The type of this distribution.
	 */
	public abstract DistributionType getType();
	
	/**
	 * Parses the given configuration XML element
	 * to the corresponding distribution.
	 * @param data The XML element to parse.
	 * @return The parsed distribution.
	 */
	public static Distribution fromXML(Element data){
		return DistributionType.getByName(data.getAttribute("type")).newInstance(data);
	}
}
