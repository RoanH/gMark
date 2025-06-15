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
		return Objects.hash(name);
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
