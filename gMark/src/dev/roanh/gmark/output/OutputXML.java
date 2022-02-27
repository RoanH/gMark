package dev.roanh.gmark.output;

import dev.roanh.gmark.util.IndentWriter;

public abstract interface OutputXML{

	public abstract void writeXML(IndentWriter writer);
	
	public default String toXML(){
		IndentWriter writer = new IndentWriter();
		writeXML(writer);
		return writer.toString();
	}
}
