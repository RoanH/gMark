package dev.roanh.gmark.query;

public class Variable{
	private int id;//TODO really just needs to be unique
	
	
	@Override
	public String toString(){
		return "?x" + id;
	}
}
