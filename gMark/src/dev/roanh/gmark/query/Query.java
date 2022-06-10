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

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

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
	 * All the bodies for this query. Note that currently
	 * only queries with a single body are ever generated.
	 */
	private List<QueryBody> bodies;
	
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
		this.bodies = Collections.singletonList(body);
		this.variables = variables;
	}
	
	/**
	 * Tests if any query body in this query has the given shape.
	 * @param shape The shape to check for.
	 * @return True if this query contains a body with the given shape.
	 */
	public boolean hasShape(QueryShape shape){
		return bodies.stream().anyMatch(body->body.getShape().equals(shape));
	}
	
	/**
	 * Gets all shapes used by the bodies in this query.
	 * @return All query body shapes.
	 */
	public Set<QueryShape> getShapes(){
		return bodies.stream().map(QueryBody::getShape).collect(Collectors.toSet());
	}
	
	/**
	 * Checks if this query contains a query body with the given selectivity.
	 * @param selectivity The selectivity to check for.
	 * @return True if this query contains a query body with the given selectivity.
	 */
	public boolean hasSelectivity(Selectivity selectivity){
		return bodies.stream().anyMatch(body->body.getSelectivity().equals(selectivity));
	}
	
	/**
	 * Gets all selectivities used by the bodies in this query.
	 * @return All query body selectivities.
	 */
	public Set<Selectivity> getSelectivities(){
		return bodies.stream().map(QueryBody::getSelectivity).collect(Collectors.toSet());
	}
	
	/**
	 * Gets the number of conjuncts in the query body with the least conjuncts.
	 * @return The minimum query body conjunct count.
	 */
	public int getMinConjuncts(){
		return bodies.stream().mapToInt(QueryBody::getConjunctCount).min().orElse(0);
	}
	
	/**
	 * Gets the number of conjuncts in the query body with the most conjuncts.
	 * @return The maximum query body conjunct count.
	 */
	public int getMaxConjuncts(){
		return bodies.stream().mapToInt(QueryBody::getConjunctCount).max().orElse(0);
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
	 * Gets all the query bodies for this query.
	 * @return All query bodies.
	 */
	public List<QueryBody> getBodies(){
		return bodies;
	}
	
	/**
	 * Gets the number of bodies that make up this query.
	 * @return The number of bodies in this query.
	 */
	public int getBodyCount(){
		return bodies.size();
	}
	
	@Override
	public String toString(){
		StringJoiner lhs = new StringJoiner(",", "(", ")");
		for(Variable var : variables){
			lhs.add(var.toString());
		}
		
		StringBuffer buffer = new StringBuffer();
		for(int i = 0; i < bodies.size(); i++){
			buffer.append(lhs.toString());
			buffer.append(" ← ");
			buffer.append(bodies.get(i).toString());
			if(i < bodies.size() - 1){
				buffer.append("\n");
			}
		}
		return buffer.toString();
	}

	@Override
	public String toSQL(){
		StringBuffer buffer = new StringBuffer();
		for(int i = 0; i < bodies.size(); i++){
			buffer.append(bodies.get(i).toSQL(variables));
			if(i < bodies.size() - 1){
				buffer.append(" UNION ");
			}
		}
		buffer.append(";");
		return buffer.toString();
	}

	@Override
	public void writeXML(IndentWriter writer){
		writer.println("<query>", 2);		
		
		writer.println("<head>", 2);
		variables.forEach(var->{
			writer.print("<var>");
			writer.print(var.toString());
			writer.println("</var>");
		});
		writer.println(2, "</head>");
		
		writer.println("<bodies>", 2);
		bodies.forEach(body->body.writeXML(writer));
		writer.println(2, "</bodies>");
		
		writer.println(2, "</query>");
	}
}
