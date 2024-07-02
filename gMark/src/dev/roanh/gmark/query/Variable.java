/*
 * gMark: A domain- and query language-independent graph instance and query workload generator.
 * Copyright (C) 2021  Roan Hofland (roan@roanh.dev).  All rights reserved.
 * GitHub Repository: https://github.com/RoanH/gMark
 *
 * gMark is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * gMark is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
		return other instanceof Variable v && v.id == id;
	}

	@Override
	public int getID(){
		return id;
	}
}
