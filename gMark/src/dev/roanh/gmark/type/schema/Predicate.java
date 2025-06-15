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
package dev.roanh.gmark.type.schema;

import java.util.Objects;

import dev.roanh.gmark.lang.QueryLanguageSyntax;
import dev.roanh.gmark.output.OutputXML;
import dev.roanh.gmark.type.IDable;
import dev.roanh.gmark.util.IndentWriter;

/**
 * Class describing predicates applied to graph
 * edges, also called symbols. These predicates
 * can also represent following an edge in its
 * inverse direction from target to source.
 * @author Roan
 */
public class Predicate implements OutputXML, Comparable<Predicate>, IDable{
	/**
	 * The unique ID of this predicate. This ID uniquely
	 * identifies this predicate among all predicates.
	 * Note that the inverse predicate has the same ID.
	 */
	private int id;
	/**
	 * The textual alias name of this predicate.
	 */
	private String alias;
	/**
	 * Fraction of all edges in the graph that
	 * have this symbol. Will be <code>null</code>
	 * if this is not specified.
	 */
	private Double proportion;
	/**
	 * True if this predicate represents the inverse
	 * of the original predicate, meaning it specifies
	 * that a directed edge should be followed in the
	 * reverse direction.
	 */
	private boolean isInverse = false;
	/**
	 * The inverse predicate object or <code>null</code>.
	 */
	private Predicate inverse = null;
	
	/**
	 * Constructs a new predicate with the given ID and alias.
	 * @param id The ID of this predicate.
	 * @param alias The alias of this predicate.
	 */
	public Predicate(int id, String alias){
		this(id, alias, null);
	}
	
	/**
	 * Constructs a new predicate with the given ID
	 * and alias and graph proportion.
	 * @param id The ID of this predicate.
	 * @param alias The alias of this predicate.
	 * @param proportion The fraction of all edges
	 *        in the graph that have this symbol.
	 *        Can be set to <code>null</code> to leave
	 *        this unspecified.
	 */
	public Predicate(int id, String alias, Double proportion){
		this.id = id;
		this.alias = alias;
		this.proportion = proportion;
	}
	
	/**
	 * Constructs an inverse predicate for the given predicate.
	 * @param predicate The predicate to invert.
	 */
	private Predicate(Predicate predicate){
		this(predicate.id, predicate.alias, predicate.proportion);
		isInverse = !predicate.isInverse;
		inverse = predicate;
	}
	
	/**
	 * True if this predicate represents the inverted
	 * form of the symbol (going from target to source).
	 * @return True if this is an inverse predicate.
	 */
	public boolean isInverse(){
		return isInverse;
	}
	
	/**
	 * Gets the textual representation
	 * for this symbol. If this predicate
	 * is inverted then this will include
	 * a super script minus character at the end.
	 * @return The predicate alias.
	 */
	public String getAlias(){
		return isInverse ? (alias + QueryLanguageSyntax.CHAR_INVERSE) : alias;
	}
	
	/**
	 * Gets the inverse predicate for this predicate. The
	 * inverse predicate basically indicates traversing an
	 * edge with the predicate in the reverse direction
	 * from target to source and is indicated by a super
	 * script minus after the predicate symbol.
	 * @return The inverse of this predicate.
	 */
	public Predicate getInverse(){
		if(inverse == null){
			inverse = new Predicate(this);
		}
		
		return inverse;
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * Uniquely identifies this predicate among all predicates.
	 * Note that the inverse predicate has the same ID.
	 * @return The unique ID of this predicate.
	 */
	@Override
	public int getID(){
		return id;
	}
	
	@Override
	public String toString(){
		return "Predicate[id=" + id + ",alias=\"" + getAlias() + "\"]";
	}
	
	@Override
	public boolean equals(Object other){
		return other instanceof Predicate p && p.id == id && p.isInverse == isInverse;
	}
	
	@Override
	public int hashCode(){
		return Objects.hash(id, isInverse);
	}

	@Override
	public void writeXML(IndentWriter writer){
		writer.print(isInverse() ? "<symbol inverse=\"true\">" : "<symbol>");
		writer.print(id);
		writer.println("</symbol>");
	}

	@Override
	public int compareTo(Predicate o){
		int c = Integer.compare(id, o.id);
		if(c == 0){
			c = Boolean.compare(isInverse, o.isInverse);
		}
		
		return c;
	}
}
