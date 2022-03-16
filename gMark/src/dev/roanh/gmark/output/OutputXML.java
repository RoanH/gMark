package dev.roanh.gmark.output;

import dev.roanh.gmark.util.IndentWriter;

/**
 * Interface for components that can be converted
 * to an XML representation.
 * @author Roan
 */
public abstract interface OutputXML{

	/**
	 * Writes this object as XML to the given writer.
	 * @param writer The writer to write to.
	 */
	public abstract void writeXML(IndentWriter writer);
	
	/**
	 * Gets the XML form of this object.
	 * @return The XML form of this object.
	 */
	public default String toXML(){
		IndentWriter writer = new IndentWriter();
		writeXML(writer);
		return writer.toString();
	}
}
