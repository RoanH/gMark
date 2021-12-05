package dev.roanh.gmark.query;

public class Variable{
	private int id;//TODO really just needs to be unique
	
	public Variable(int id){
		this.id = id;
	}
	
	@Override
	public String toString(){
		return "?x" + id;
	}
}
