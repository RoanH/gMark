package dev.roanh.gmark.output;

public interface SQL{

	//has to be wrapped in () at the outer level and select a src-trg pair table
	public abstract String toSQL();
}
