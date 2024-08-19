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
package dev.roanh.gmark.lang.rpq;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.roanh.gmark.core.graph.Predicate;
import dev.roanh.gmark.lang.QueryLanguage;
import dev.roanh.gmark.lang.generic.GenericParser;

/**
 * Parser for RPQs (Regular Path Queries).
 * @author Roan
 * @see RPQ
 */
public final class ParserRPQ extends GenericParser{
	
	/**
	 * Prevent instantiation.
	 */
	private ParserRPQ(){
	}
	
	/**
	 * Parses the given RPQ in string form to an RPQ instance. The input is assumed
	 * to use brackets where possible and to use the '{@value QueryLanguage#CHAR_CUP}',
	 * '{@value QueryLanguage#CHAR_JOIN}', '{@value QueryLanguage#CHAR_KLEENE}' and
	 * '{@value QueryLanguage#CHAR_INVERSE}' symbols to denote operations.
	 * Example input: {@code (0◦(((1◦0) ∪ (1◦1))◦1⁻))}.
	 * @param query The RPQ to parse.
	 * @return The parsed RPQ.
	 * @throws IllegalArgumentException When the given string is not a valid RPQ.
	 * @see ParserRPQ#parse(String, char, char, char, char)
	 */
	public static RPQ parse(String query) throws IllegalArgumentException{
		return parse(query, QueryLanguage.CHAR_JOIN, QueryLanguage.CHAR_CUP, QueryLanguage.CHAR_KLEENE, QueryLanguage.CHAR_INVERSE);
	}
	
	/**
	 * Parses the given RPQ in string form to an RPQ instance. Unlike
	 * {@link #parse(String)} this subroutine allows custom symbols
	 * to be used to input the RPQ.
	 * @param query The RPQ to parse.
	 * @param join The symbol to use for the join/concatenation operation.
	 * @param disjunct The symbol to use for the disjunction operation.
	 * @param kleene The symbol to use for the kleene/transitive closure operation.
	 * @param inverse The symbol to use for the inverse edge label operation.
	 * @return The parsed RPQ.
	 * @throws IllegalArgumentException When the given string is not a valid RPQ.
	 * @see #parse(String)
	 */
	public static RPQ parse(String query, char join, char disjunct, char kleene, char inverse) throws IllegalArgumentException{
		return parse(query, new HashMap<String, Predicate>(), join, disjunct, kleene, inverse);
	}

	/**
	 * Parses the given RPQ in string form to an RPQ instance. Unlike
	 * {@link #parse(String)} this subroutine allows custom symbols
	 * to be used to input the RPQ.
	 * @param query The RPQ to parse.
	 * @param labels A map with predicates found so far.
	 * @param join The symbol to use for the join/concatenation operation.
	 * @param disjunct The symbol to use for the disjunction operation.
	 * @param kleene The symbol to use for the kleene/transitive closure operation.
	 * @param inverse The symbol to use for the inverse edge label operation.
	 * @return The parsed RPQ.
	 * @throws IllegalArgumentException When the given string is not a valid RPQ.
	 * @see #parse(String)
	 */
	public static RPQ parse(String query, Map<String, Predicate> labels, char join, char disjunct, char kleene, char inverse) throws IllegalArgumentException{
		List<String> parts = split(query, join);
		if(parts.size() > 1){
			return RPQ.concat(parts.stream().map(part->{
				return parse(part, labels, join, disjunct, kleene, inverse);
			}).toList());
		}
		
		parts = split(query, disjunct);
		if(parts.size() > 1){
			return RPQ.disjunct(parts.stream().map(part->{
				return parse(part, labels, join, disjunct, kleene, inverse);
			}).toList());
		}
		
		if(query.startsWith("(") && query.endsWith(")")){
			return parse(query.substring(1, query.length() - 1), labels, join, disjunct, kleene, inverse);
		}
		
		if(query.endsWith(String.valueOf(kleene))){
			return RPQ.kleene(parse(query.substring(0, query.length() - 1), labels, join, disjunct, kleene, inverse));
		}
		
		if(query.indexOf('(') == -1 && query.indexOf(')') == -1 && query.indexOf(join) == -1 && query.indexOf(disjunct) == -1 && query.indexOf(kleene) == -1){
			return RPQ.label(parsePredicate(query, labels, inverse));
		}

		throw new IllegalArgumentException("Invalid RPQ.");
	}
}
