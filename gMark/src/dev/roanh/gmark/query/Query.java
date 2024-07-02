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

import java.util.List;
import java.util.StringJoiner;

import dev.roanh.gmark.core.QueryShape;
import dev.roanh.gmark.core.Selectivity;
import dev.roanh.gmark.output.OutputSQL;
import dev.roanh.gmark.output.OutputXML;
import dev.roanh.gmark.util.IndentWriter;

/**
 * Represents a generated query with a number of
 * projected variables and one or more query bodies.
 * @author Roan
 */
public class Query implements OutputSQL, OutputXML{
	/**
	 * The projected variables for this query (head, left hand side).
	 */
	private List<Variable> variables;
	/**
	 * The body for this query.
	 */
	private QueryBody body;
	
	/**
	 * Constructs a new query with a single body made up of the given
	 * list of conjuncts and with the given project variables, selectivity and shape.
	 * @param conjuncts The conjuncts for the query body.
	 * @param variables The projected query variables.
	 * @param selectivity The query body selectivity.
	 * @param shape The query body shape.
	 */
	public Query(List<Conjunct> conjuncts, List<Variable> variables, Selectivity selectivity, QueryShape shape){
		this(new QueryBody(conjuncts, selectivity, shape), variables);
	}
	
	/**
	 * Constructs a new query with a single body and with the
	 * given project variables, selectivity and shape.
	 * @param body The query body.
	 * @param variables The projected query variables.
	 */
	public Query(QueryBody body, List<Variable> variables){
		this.body = body;
		this.variables = variables;
	}
	
	/**
	 * Gets the shape of this query.
	 * @return The query shape.
	 */
	public QueryShape getShapes(){
		return body.getShape();
	}
	
	/**
	 * Gets the selectivity of this query.
	 * @return The selectivity of this query.
	 */
	public Selectivity getSelectivity(){
		return body.getSelectivity();
	}
	
	/**
	 * Gets the number of conjuncts in the query body with the most conjuncts.
	 * @return The maximum query body conjunct count.
	 */
	public int getConjunctCount(){
		return body.getConjunctCount();
	}
	
	/**
	 * Checks if this query is binary. A query is binary when
	 * its arity is equal to 0.
	 * @return True if this query is binary.
	 */
	public boolean isBinary(){
		return getArity() == 0;
	}
	
	/**
	 * Gets the arity (number of projected variables) for this query.
	 * @return The arity for this query.
	 */
	public int getArity(){
		return variables.size();
	}
	
	/**
	 * Gets the query body for this query.
	 * @return The query body.
	 */
	public QueryBody getBody(){
		return body;
	}
	
	@Override
	public void writeSQL(IndentWriter writer){
		body.writeSQL(writer, variables);
	}
	
	@Override
	public String toString(){
		StringJoiner lhs = new StringJoiner(",", "(", ")");
		for(Variable v : variables){
			lhs.add(v.toString());
		}
		
		StringBuilder buffer = new StringBuilder();
		buffer.append(lhs.toString());
		buffer.append(" ← ");
		buffer.append(body.toString());
		return buffer.toString();
	}
	
	@Override
	public void writeXML(IndentWriter writer){
		writer.println("<query>", 2);		
		
		writer.println("<head>", 2);
		variables.forEach(v->{
			writer.print("<var>");
			writer.print(v.toString());
			writer.println("</var>");
		});
		writer.println(2, "</head>");
		
		writer.println("<bodies>", 2);
		body.writeXML(writer);
		writer.println(2, "</bodies>");
		
		writer.println(2, "</query>");
	}
}
