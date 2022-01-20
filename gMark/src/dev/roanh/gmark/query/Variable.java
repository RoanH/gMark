package dev.roanh.gmark.query;

import java.util.Objects;

public class Variable{
	private int id;//TODO really just needs to be unique
	
	public Variable(int id){
		this.id = id;
	}
	
	@Override
	public String toString(){
		return "?x" + id;
	}
	
	@Override
	public int hashCode(){
		return Objects.hash(id);
	}
	
	@Override
	public boolean equals(Object other){
		return other instanceof Variable ? ((Variable)other).id == id : false;
	}
}
