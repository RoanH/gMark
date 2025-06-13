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

import static dev.roanh.gmark.lang.QueryLanguageSyntax.CHAR_ASSIGN;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.roanh.gmark.lang.generic.GenericParser;
import dev.roanh.gmark.type.schema.Predicate;

public final class ParserCQ extends GenericParser{
	
	/**
	 * Prevent instantiation.
	 */
	private ParserCQ(){
	}
	
	public static CQ parse(String query) throws IllegalArgumentException{
		return parse(query, CHAR_ASSIGN);
	}
	
	public static CQ parse(String query, List<Predicate> labels) throws IllegalArgumentException{
		return parse(query, labels, CHAR_ASSIGN);
	}
	
	public static CQ parse(String query, List<Predicate> labels, char assign) throws IllegalArgumentException{
		return parse(query, mapPredicates(labels), assign);
	}
	
	public static CQ parse(String query, char assign) throws IllegalArgumentException{
		return parse(query, new HashMap<String, Predicate>(), assign);
	}
	
	private static CQ parse(String query, Map<String, Predicate> labels, char assign) throws IllegalArgumentException{
		CQ cq = CQ.empty();
		
		query = query.trim();
		int assignIdx = query.indexOf(assign);
		if(assignIdx == -1 || assignIdx == query.length() - 1){
			throw new IllegalArgumentException("Invalid CQ, head or body absent.");
		}
		
		//read projected free variables
		String head = query.substring(0, assignIdx).trim();
		if(head.startsWith("(") && head.endsWith(")")){
			head = head.substring(1, head.length() - 1);
		}else{
			throw new IllegalArgumentException("Invalid CQ, head format invalid.");
		}
		
		Map<String, VarCQ> variables = new HashMap<String, VarCQ>();
		for(String freeVar : split(head, ',')){
			variables.computeIfAbsent(freeVar, cq::addFreeVariable);
		}
		
		//atoms
		for(String atom : split(query.substring(assignIdx + 1), ',')){
			int open = atom.indexOf('(');
			if(open == -1 || !atom.endsWith(")")){
				throw new IllegalArgumentException("Invalid CQ, atom format invalid.");
			}
			
			List<String> vars = split(atom.substring(open + 1, atom.length() - 1), ',');
			if(vars.size() != 2){
				throw new IllegalArgumentException("Invalid CQ, atom does not have exactly two variables.");
			}
			
			cq.addAtom(
				variables.computeIfAbsent(vars.get(0), cq::addBoundVariable),
				parsePredicate(atom.substring(0, open).trim(), labels, (char)0),
				variables.computeIfAbsent(vars.get(1), cq::addBoundVariable)
			);
		}

		return cq;
	}
}
