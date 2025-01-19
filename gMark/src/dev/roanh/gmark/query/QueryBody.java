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
package dev.roanh.gmark.query;

import java.util.List;
import java.util.StringJoiner;

import dev.roanh.gmark.gen.shape.QueryShape;
import dev.roanh.gmark.output.OutputXML;
import dev.roanh.gmark.type.Selectivity;
import dev.roanh.gmark.util.IndentWriter;

/**
 * Represents the body of a query, this is essentially
 * the query without the projected head variables.
 * @author Roan
 * @see Query
 */
public class QueryBody implements OutputXML{
	/**
	 * The conjuncts in this query body.
	 */
	private List<Conjunct> conjuncts;
	/**
	 * The selectivity of this query body.
	 */
	private Selectivity selectivity;
	/**
	 * The shape of this query body.
	 */
	private QueryShape shape;
	
	/**
	 * Constructs a new query body with the given conjuncts, selectivity and shape.
	 * @param conjuncts The conjuncts for this query body.
	 * @param selectivity The selectivity of this query body.
	 * @param shape The shape of this query body.
	 * @throws IllegalArgumentException When the list of conjuncts is empty.
	 */
	public QueryBody(List<Conjunct> conjuncts, Selectivity selectivity, QueryShape shape) throws IllegalArgumentException{
		this.selectivity = selectivity;
		this.shape = shape;
		this.conjuncts = conjuncts;
		if(conjuncts.isEmpty()){
			throw new IllegalArgumentException("Query body cannot have no conjuncts.");
		}
	}
	
	/**
	 * Gets the conjuncts for this query body.
	 * @return The conjuncts for this query body.
	 */
	public List<Conjunct> getConjuncts(){
		return conjuncts;
	}
	
	/**
	 * Gets the number of conjuncts in this query body.
	 * @return The number of conjuncts in this query body.
	 */
	public int getConjunctCount(){
		return conjuncts.size();
	}
	
	/**
	 * Gets the multiplicity of this query body. This is computed
	 * as the fraction of all conjuncts that have a Kleene star above it.
	 * @return The multiplicity of this query body.
	 */
	public double getMultiplicity(){
		long stars = conjuncts.stream().filter(Conjunct::hasStar).count();
		return ((double)stars) / conjuncts.size();
	}
	
	/**
	 * Gets the selectivity of this query body.
	 * @return The selectivity of this query body.
	 */
	public Selectivity getSelectivity(){
		return selectivity;
	}
	
	/**
	 * Gets the shape of this query body.
	 * @return The shape of this query body.
	 */
	public QueryShape getShape(){
		return shape;
	}
	
	@Override
	public String toString(){
		StringJoiner joiner = new StringJoiner(",");
		for(Conjunct conj : conjuncts){
			joiner.add(conj.toString());
		}
		return joiner.toString();
	}
	
	/**
	 * Writes the SQL form of this query body to the given writer.
	 * @param writer The writer to write to.
	 */
	protected void writeSQL(IndentWriter writer){
		writer.println("WITH RECURSIVE");
		for(int i = 0; i < conjuncts.size(); i++){
			Conjunct conj = conjuncts.get(i);
			conj.writeSQL(writer, "c" + i);
			if(i < conjuncts.size() - 1){
				writer.println(",");
			}
		}
	}
	
	@Override
	public void writeXML(IndentWriter writer){
		writer.println("<body>", 2);
		conjuncts.forEach(conj->conj.writeXML(writer));
		writer.println(2, "</body>");
	}
}
