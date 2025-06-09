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

import dev.roanh.gmark.ast.EdgeQueryAtom;
import dev.roanh.gmark.ast.QueryVariable;
import dev.roanh.gmark.output.OutputFormal;
import dev.roanh.gmark.output.OutputSQL;
import dev.roanh.gmark.output.OutputXML;
import dev.roanh.gmark.type.schema.Predicate;
import dev.roanh.gmark.util.IndentWriter;

/**
 * Simple query action of traversing an edge with
 * a single label from either source to target or
 * in inverse direction from target to source.
 * @author Roan
 */
public abstract class GenericEdge implements OutputSQL, OutputFormal, OutputXML, EdgeQueryAtom{
	/**
	 * The label traversed by this query.
	 */
	protected final Predicate symbol;

	/**
	 * Constructs a new generic edge with the given label to traverse.
	 * @param symbol The label to traverse.
	 */
	protected GenericEdge(Predicate symbol){
		this.symbol = symbol;
	}
	
	@Override
	public QueryVariable getSource(){
		return GenericVariable.SOURCE;
	}
	
	@Override
	public QueryVariable getTarget(){
		return GenericVariable.TARGET;
	}
	
	@Override
	public Predicate getLabel(){
		return symbol;
	}
	
	@Override
	public String toFormalSyntax(){
		return symbol.getAlias();
	}
	
	@Override
	public String toString(){
		return toFormalSyntax();
	}
	
	@Override
	public void writeSQL(IndentWriter writer){
		if(symbol.isInverse()){
			writer.print("SELECT trg AS src, src AS trg FROM edge WHERE label = " + symbol.getID());
		}else{
			writer.print("SELECT src, trg FROM edge WHERE label = " + symbol.getID());
		}
	}
	
	@Override
	public void writeXML(IndentWriter writer){
		//TODO validate
		symbol.writeXML(writer);
	}
}
