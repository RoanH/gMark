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

import dev.roanh.gmark.core.WorkloadType;
import dev.roanh.gmark.output.OutputXML;
import dev.roanh.gmark.util.IndentWriter;

/**
 * Abstract base class for query conjuncts.
 * @author Roan
 */
public abstract class Conjunct implements OutputXML{
	/**
	 * The source variable for this conjunct.
	 */
	private Variable source;
	/**
	 * The target variable for this conjunct.
	 */
	private Variable target;
	/**
	 * True if this conjunct has a Kleene star above it.
	 */
	private boolean star;
	
	/**
	 * Constructs a new conjunct with the given source
	 * variable, target variable and Kleene star status.
	 * @param source The conjunct source variable.
	 * @param target The conjunct target variable.
	 * @param star True if the conjunct has a Kleene star above it.
	 */
	protected Conjunct(Variable source, Variable target, boolean star){
		this.source = source;
		this.target = target;
		this.star = star;
	}
	
	/**
	 * Gets if this conjunct has a Kleene star above it.
	 * @return True if this conjunct has a Kleene star above it.
	 */
	public boolean hasStar(){
		return star;
	}
	
	/**
	 * Gets the source variable for this conjunct.
	 * @return The source variable for this conjunct.
	 */
	public Variable getSource(){
		return source;
	}
	
	/**
	 * Gets the target variable for this conjunct.
	 * @return The target variable for this conjunct.
	 */
	public Variable getTarget(){
		return target;
	}
	
	/**
	 * Gets the string representation of the inner
	 * part of this conjunct.
	 * @return The inner string for this conjunct.
	 */
	protected abstract String getInnerString();
	
	/**
	 * Writes the SQL representation of the inner part
	 * of this conjunct to the given writer.
	 * @param writer The writer to write to.
	 */
	protected abstract void writePartialSQL(IndentWriter writer);
	
	/**
	 * Writes the XML representation of the inner part
	 * of this conjunct to the given writer.
	 * @param writer The writer to write to.
	 */
	protected abstract void writePartialXML(IndentWriter writer);
	
	/**
	 * Gets the workload type this conjunct belongs to.
	 * @return The workload type this conjunct belongs to.
	 */
	public abstract WorkloadType getType();
	
	/**
	 * Writes the SQL form of this conjunct clause to the given writer.
	 * @param writer The writer to write to.
	 * @param conjunctBaseName The base name of the conjunct clause, this
	 *        name will be prefixed with 'c' and optionally a second clause
	 *        will be written that is prefixed with 'c' and suffixed with 'tc'
	 *        if this conjunct has a Kleene star.
	 * @see #hasStar()
	 */
	protected void writeSQL(IndentWriter writer, String conjunctBaseName){
		writer.print(conjunctBaseName);
		writer.println("(src, trg) AS (", 2);
		if(hasStar()){
			writer.println("SELECT edge.src, edge.src");
			writer.println("FROM edge");
			writer.println("UNION");
			writer.println("SELECT edge.trg, edge.trg");
			writer.println("FROM edge");
			writer.println("UNION");
		}
		
		writePartialSQL(writer);
		writer.println();
		writer.decreaseIndent(2);
		writer.print(")");
		
		if(hasStar()){
			writer.println(",");
			writer.print(conjunctBaseName);
			writer.println("tc(src, trg) AS (", 2);
			writer.println("SELECT src, trg");
			writer.println("FROM c" + conjunctBaseName);
			writer.println("UNION");
			writer.println("SELECT head.src, tail.trg");
			writer.println("FROM c" + conjunctBaseName + " AS head, c" + conjunctBaseName + "tc AS tail");
			writer.println("WHERE head.trg = tail.src");
			writer.decreaseIndent(2);
			writer.print(")");
		}
	}
	
	@Override
	public String toString(){
		if(star){
			return "(" + source + "," + getInnerString() + "*," + target + ")";
		}else{
			return "(" + source + "," + getInnerString() + "," + target + ")";
		}
	}
	
	@Override
	public void writeXML(IndentWriter writer){
		writer.print("<conjunct src=\"");
		writer.print(source.toString());
		writer.print("\" trg=\"");
		writer.print(target.toString());
		writer.print("\" type=\"");
		writer.print(getType().getID());
		writer.println("\">", 2);
		if(star){
			writer.println("<star>", 2);
		}
		
		writePartialXML(writer);
		if(star){
			writer.println("</star>", 2);
		}
		
		writer.println(2, "</conjunct>");
	}
}
