package dev.roanh.gmark.output;

public abstract interface OutputSQL extends OutputFormat{

	//TODO indent writer
	//has to be wrapped in () at the outer level and select a src-trg pair table
	public abstract String toSQL();
}
