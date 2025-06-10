package dev.roanh.gmark.lang.cq;

import java.util.Objects;

import dev.roanh.gmark.ast.QueryVariable;
import dev.roanh.gmark.output.OutputXML;
import dev.roanh.gmark.util.IndentWriter;

/**
 * Representation of a variable in a CQ.
 * @author Roan
 */
public class VarCQ implements QueryVariable, OutputXML{
	/**
	 * The display name of this variable.
	 */
	private final String name;
	/**
	 * True if this is a free variable, free variables
	 * are projected in the output of a query.
	 */
	private final boolean free;
	
	/**
	 * Constructs a new CQ variable based on the given variable.
	 * @param variable The variable to use as a template.
	 */
	public VarCQ(QueryVariable variable){
		this(variable.getName(), variable.isFree());
	}
	
	/**
	 * Constructs a new CQ variable.
	 * @param name The name of this variable.
	 * @param free True if this is a projected free variable.
	 */
	public VarCQ(String name, boolean free){
		this.name = name;
		this.free = free;
	}
	
	@Override
	public String getName(){
		return name;
	}
	
	@Override
	public boolean isFree(){
		return free;
	}
	
	@Override
	public String toString(){
		return getName();
	}
	
	@Override
	public boolean equals(Object obj){
		return obj instanceof VarCQ v && Objects.equals(v.name, name);
	}
	
	@Override
	public int hashCode(){
		return Objects.hash(name, free);
	}
	
	@Override
	public void writeXML(IndentWriter writer){
		writer.print("<var free=");
		writer.print(free);
		writer.print(">");
		writer.print(name);
		writer.println("</var>");
	}
}
