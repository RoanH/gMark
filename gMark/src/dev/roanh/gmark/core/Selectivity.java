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
package dev.roanh.gmark.core;

import dev.roanh.gmark.util.IDable;

/**
 * Enum with all possible selectivity values. The
 * selectivity value shows how the number of results
 * returned by a query grows when the size of the graph grows.
 * @author Roan
 */
public enum Selectivity implements IDable{
	/**
	 * This means a query returns about the same number
	 * of results regardless of how large the graph becomes.
	 * Typically this is seen for queries that select nodes
	 * that are present in a fixed quantity in the graph.
	 */
	CONSTANT(0, "constant"),
	/**
	 * The most common selectivity value. This indicates
	 * queries that select more results when the graph grows
	 * in size and the number of results select grows in
	 * a linear fashion with the graph size.
	 */
	LINEAR(1, "linear"),
	/**
	 * This selectivity value indicates that the number of
	 * results selected by a query grows much faster than
	 * the size of the graph itself. This selectivity value
	 * is typically seen for queries that perform a Cartesian
	 * product.
	 */
	QUADRATIC(2, "quadratic");
	
	/**
	 * The numerical ID of this selectivity.
	 */
	private final int id;
	/**
	 * The name of this selectivity (as used in configuration files).
	 */
	private final String name;
	
	/**
	 * Constructs a new selectivity with the given name.
	 * @param id The numerical ID of this selectivity (as used in workload XMLs).
	 * @param name The selectivity name (as used in configuration files).
	 */
	private Selectivity(int id, String name){
		this.id = id;
		this.name = name;
	}
	
	/**
	 * Gets the name of this selectivity. This is the same
	 * name that is also used for configuration files.
	 * @return The name of this selectivity.
	 */
	public String getName(){
		return name;
	}
	
	@Override
	public String toString(){
		return name;
	}

	@Override
	public int getID(){
		return id;
	}
	
	/**
	 * Gets the selectivity identified by the given name.
	 * This name is also used for configuration files.
	 * @param name The name of the selectivity to get.
	 * @return The selectivity with the given name or
	 *         <code>null</code> if no selectivity with
	 *         the given name was found.
	 */
	public static final Selectivity getByName(String name){
		for(Selectivity sel : values()){
			if(sel.name.equalsIgnoreCase(name)){
				return sel;
			}
		}
		return null;
	}
}
