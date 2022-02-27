package dev.roanh.gmark.conjunct.cpq;

import dev.roanh.gmark.output.OutputSQL;
import dev.roanh.gmark.output.OutputXML;
import dev.roanh.gmark.util.IndentWriter;

public abstract interface CPQ extends OutputSQL, OutputXML{
	public static final CPQ IDENTITY = new CPQ(){
		@Override
		public String toString(){
			return "id";
		}

		@Override
		public String toSQL(){
			throw new IllegalStateException("Indentity to OutputSQL not supported (and never generated).");
		}

		@Override
		public void writeXML(IndentWriter writer){
			writer.println("<cpq type=\"identity\"></cpq>");
		}
	};
}
