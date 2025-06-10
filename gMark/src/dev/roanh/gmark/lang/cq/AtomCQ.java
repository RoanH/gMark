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

import dev.roanh.gmark.ast.EdgeQueryAtom;
import dev.roanh.gmark.output.OutputXML;
import dev.roanh.gmark.type.schema.Predicate;
import dev.roanh.gmark.util.IndentWriter;

/**
 * CQ query language atom, i.e., the selection of single graph edges.
 * @author Roan
 */
public class AtomCQ implements EdgeQueryAtom, OutputXML{
	/**
	 * The edge source vertex result variable.
	 */
	private final VarCQ source;
	/**
	 * The label for the edges to select.
	 */
	private final Predicate label;
	/**
	 * The edge target vertex result variable.
	 */
	private final VarCQ target;
	
	/**
	 * Constructs a new CQ atom.
	 * @param source The source variable.
	 * @param label The edge label.
	 * @param target The target variable.
	 */
	public AtomCQ(VarCQ source, Predicate label, VarCQ target){
		this.source = source;
		this.label = label;
		this.target = target;
	}
	
	public boolean contains(VarCQ v){
		return source.equals(v) || target.equals(v);
	}
	
	@Override
	public VarCQ getSource(){
		return source;
	}
	
	@Override
	public VarCQ getTarget(){
		return target;
	}
	
	@Override
	public Predicate getLabel(){
		return label;
	}
	
	@Override
	public String toString(){
		return label.getAlias() + "(" + source.getName() + ", " + target.getName() + ")";
	}
	
	@Override
	public boolean equals(Object obj){
		return obj instanceof AtomCQ atom && Objects.equals(atom.source, source) && Objects.equals(atom.label, label) && Objects.equals(atom.target, target);
	}
	
	@Override
	public int hashCode(){
		return Objects.hash(source, label, target);
	}
	
	@Override
	public void writeXML(IndentWriter writer){
		writer.println("<atom>", 2);
		source.writeXML(writer);
		label.writeXML(writer);
		target.writeXML(writer);
		writer.println(2, "</atom>");
	}
}
