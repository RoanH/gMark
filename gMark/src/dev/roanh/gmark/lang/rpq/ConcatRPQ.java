package dev.roanh.gmark.lang.rpq;

import java.util.List;

import dev.roanh.gmark.lang.generic.GenericConcatenation;
import dev.roanh.gmark.util.IndentWriter;

public class ConcatRPQ extends GenericConcatenation<RPQ> implements RPQ{

	public ConcatRPQ(List<RPQ> elements) throws IllegalArgumentException{
		super(elements);
	}
	
	@Override
	public void writeXML(IndentWriter writer){
		writer.println("<rpq type=\"concat\">", 2);
		elements.forEach(c->c.writeXML(writer));
		writer.println(2, "</rpq>");
	}
}
