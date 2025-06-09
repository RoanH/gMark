/*
 * gMark: A domain- and query language-independent query workload generator and query language utility library.
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
package dev.roanh.gmark.lang.generic;

import dev.roanh.gmark.ast.QueryVariable;

/**
 * Generic variables for reachability queries.
 * @author Roan
 * @see #SOURCE
 * @see #TARGET
 */
public final class GenericVariable implements QueryVariable{
	/**
	 * Generic reachability query source variable.
	 */
	public static final QueryVariable SOURCE = new GenericVariable("src");
	/**
	 * Generic reachability query target variable.
	 */
	public static final QueryVariable TARGET = new GenericVariable("trg");
	/**
	 * The display name of this variable.
	 */
	private final String name;
	
	/**
	 * Constructs a new generic variable with the given name.
	 * @param name The display name for this variable.
	 */
	private GenericVariable(String name){
		this.name = name;
	}
	
	@Override
	public String getName(){
		return name;
	}

	@Override
	public boolean isFree(){
		return true;
	}
}
