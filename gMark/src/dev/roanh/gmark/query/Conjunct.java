package dev.roanh.gmark.query;

import dev.roanh.gmark.core.WorkloadType;
import dev.roanh.gmark.output.OutputXML;
import dev.roanh.gmark.util.IndentWriter;

public abstract class Conjunct implements OutputXML{
	private Variable source;
	private Variable target;
	private boolean star;
	
//	protected Conjunct(Variable source, Variable target, boolean star){
//		this.source = source;
//		this.target = target;
//		this.star = star;
//	}
	
	//TODO this relies on the constructing party to set the data, not very nice, use a builder?
	public void setData(Variable source, Variable target, boolean star){
		this.source = source;
		this.target = target;
		this.star = star;
	}
	
	public boolean hasStar(){
		return star;
	}
	
	public Variable getSource(){
		return source;
	}
	
	public Variable getTarget(){
		return target;
	}
	
	//TODO should probably not be a thing
	protected abstract String getInnerString();
	
	//does not have to respect star
	protected abstract String toPartialSQL();
	
	protected abstract void writePartialXML(IndentWriter writer);
	
	public abstract WorkloadType getType();
	
	@Override
	public String toString(){
		return "(" + source + "," + getInnerString() + "," + target + ")";
	}

	@Override
	public void writeXML(IndentWriter writer){
		writer.print("<conjunct src=\"");
		writer.print(source.toString());
		writer.print("\" trg=\"");
		writer.print(target.toString());
		writer.print("\" type=\"");
		writer.print(getType().getID());
		writer.println("\">", 2);
		
		writePartialXML(writer);
		writer.println(2, "</conjunct>");
	}
}
