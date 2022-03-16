package dev.roanh.gmark.query;

import java.util.Objects;

import dev.roanh.gmark.util.IDable;

/**
 * Represents a query variable.
 * @author Roan
 */
public class Variable implements IDable{
	/**
	 * The numerical ID of this variable.
	 */
	private int id;
	
	/**
	 * Constructs a new variable with the given
	 * unique numerical ID.
	 * @param id The ID of this variable.
	 */
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

	@Override
	public int getID(){
		return id;
	}
}
