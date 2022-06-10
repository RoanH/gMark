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
package dev.roanh.gmark.core.graph;

import java.util.Objects;

import dev.roanh.gmark.util.IDable;

/**
 * Represents a single type in a graph schema.
 * A type encoded information about the number
 * of nodes of that specific type in a concrete
 * graph instance.
 * @author Roan
 */
public class Type implements IDable{
	/**
	 * The ID of this type.
	 */
	private final int id;
	/**
	 * The textual alias name for this type.
	 */
	private String alias;
	/**
	 * If this nodes of this type are present in
	 * a fixed quantity in a concrete graph instance
	 * then this is the number of nodes of this
	 * type present in the graph. Otherwise this
	 * value is <code>null</code>.
	 */
	private Integer fixed;
	/**
	 * If this nodes of this type are present as
	 * a proportion of all nodes in a concrete
	 * graph instance then this is the fraction
	 * of nodes of this type present in the graph.
	 * Otherwise this value is <code>null</code>.
	 */
	private Double proportion;
	
	/**
	 * Constructs a new type with the given id, alias
	 * and fixed presence quantity.
	 * @param id The ID of this type.
	 * @param alias The alias name of this type.
	 * @param fixed The number of nodes in a concrete
	 *        graph instance that are of this type.
	 */
	public Type(int id, String alias, int fixed){
		this.id = id;
		this.alias = alias;
		this.fixed = fixed;
		proportion = null;
	}
	
	/**
	 * Constructs a new type with the given id, alias
	 * and proportional presence quantity.
	 * @param id The ID of this type.
	 * @param alias The alias name of this type.
	 * @param proportion The fraction of all nodes
	 *        in a concrete graph instance that are of this type.
	 */
	public Type(int id, String alias, double proportion){
		this.id = id;
		this.alias = alias;
		this.proportion = proportion;
		fixed = null;
	}
	
	/**
	 * Checks if nodes of this type scale in number
	 * with the size of the graph. If this is false
	 * then nodes of this type are instead present
	 * in a fixed quantity.
	 * @return True if nodes of this type are scalable.
	 */
	public boolean isScalable(){
		return fixed == null;
	}
	
	/**
	 * If this nodes of this type are present in
	 * a fixed quantity in a concrete graph instance
	 * then this gets the number of nodes of this
	 * type present in the graph.
	 * @return The number of nodes in a graph instance
	 *         of this type.
	 * @throws IllegalStateException If this type
	 *         is not present in a fixed quantity.
	 * @see #isScalable()
	 */
	public int getNodeCount(){
		if(fixed == null){
			throw new IllegalStateException("Type is not present in a fixed quantity.");
		}
		return fixed;
	}
	
	/**
	 * If this nodes of this type are present as
	 * a proportion of all nodes in a concrete
	 * graph instance then this gets the fraction
	 * of nodes of this type present in the graph.
	 * @return The fraction of all nodes in a graph
	 *         that are of this type.
	 * @throws IllegalStateException If this type
	 *         is not present in a proportional quantity.
	 * @see #isScalable()
	 */
	public double getProportion(){
		if(proportion == null){
			throw new IllegalStateException("Type is not present in a propotional quantity.");
		}
		return proportion;
	}
	
	/**
	 * Gets the textual alias name for this type.
	 * @return The alias for this type.
	 */
	public String getAlias(){
		return alias;
	}
	
	@Override
	public int getID(){
		return id;
	}
	
	@Override
	public String toString(){
		if(fixed == null){
			return "Type[typeID=" + id + ",alias=\"" + alias + "\",proportion=" + proportion + "]";
		}else{
			return "Type[typeID=" + id + ",alias=\"" + alias + "\",fixed=" + fixed + "]";
		}
	}
	
	@Override
	public boolean equals(Object other){
		return other instanceof Type ? ((Type)other).id == id : false;
	}
	
	@Override
	public int hashCode(){
		return Objects.hash(id);
	}
}
