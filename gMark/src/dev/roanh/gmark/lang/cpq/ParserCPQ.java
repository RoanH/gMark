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
package dev.roanh.gmark.lang.cpq;

import static dev.roanh.gmark.lang.QueryLanguageSyntax.CHAR_INTERSECTION;
import static dev.roanh.gmark.lang.QueryLanguageSyntax.CHAR_INVERSE;
import static dev.roanh.gmark.lang.QueryLanguageSyntax.CHAR_JOIN;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.roanh.gmark.lang.QueryLanguageSyntax;
import dev.roanh.gmark.lang.generic.GenericParser;
import dev.roanh.gmark.type.schema.Predicate;

/**
 * Parser for CPQs (Conjunctive Path Queries).
 * @author Roan
 * @see CPQ
 */
public final class ParserCPQ extends GenericParser{
	
	/**
	 * Prevent instantiation.
	 */
	private ParserCPQ(){
	}
	
	/**
	 * Parses the given CPQ in string form to a CPQ instance. The input is assumed
	 * to use brackets where possible and to use the '{@code id}', '{@value QueryLanguageSyntax#CHAR_JOIN}',
	 * '{@value QueryLanguageSyntax#CHAR_INTERSECTION}' and '{@value QueryLanguageSyntax#CHAR_INVERSE}' symbols to denote
	 * operations. Example input: {@code (0◦(((1◦0) ∩ (1◦1))◦1⁻))}.
	 * @param query The CPQ to parse.
	 * @return The parsed CPQ.
	 * @throws IllegalArgumentException When the given string is not a valid CPQ.
	 * @see QueryLanguageSyntax
	 */
	public static CPQ parse(String query) throws IllegalArgumentException{
		return parse(query, CHAR_JOIN, CHAR_INTERSECTION, CHAR_INVERSE);
	}

	/**
	 * Parses the given CPQ in string form to a CPQ instance. The input is assumed
	 * to use brackets where possible and to use the '{@code id}', '{@value QueryLanguageSyntax#CHAR_JOIN}',
	 * '{@value QueryLanguageSyntax#CHAR_INTERSECTION}' and '{@value QueryLanguageSyntax#CHAR_INVERSE}' symbols to denote
	 * operations. Example input: {@code (0◦(((1◦0) ∩ (1◦1))◦1⁻))}.
	 * @param query The CPQ to parse.
	 * @param labels The label set to use, new labels will <b>not</b> be created if labels are found in the
	 *        input that are not covered by the given list.
	 * @return The parsed CPQ.
	 * @throws IllegalArgumentException When the given string is not a valid CPQ or contains unknown labels.
	 * @see QueryLanguageSyntax
	 */
	public static CPQ parse(String query, List<Predicate> labels) throws IllegalArgumentException{
		return parse(query, labels, CHAR_JOIN, CHAR_INTERSECTION, CHAR_INVERSE);
	}
	
	/**
	 * Parses the given CPQ in string form to a CPQ instance using the given syntax symbols.
	 * @param query The CPQ to parse.
	 * @param join The symbol to use for the join/concatenation operation.
	 * @param intersect The symbol to use for the intersection/conjunction operation.
	 * @param inverse The symbol to use for the inverse edge label operation.
	 * @return The parsed CPQ.
	 * @throws IllegalArgumentException When the given string is not a valid CPQ.
	 */
	public static CPQ parse(String query, char join, char intersect, char inverse) throws IllegalArgumentException{
		return parse(query, new HashMap<String, Predicate>(), join, intersect, inverse);
	}
	
	/**
	 * Parses the given CPQ in string form to a CPQ instance using the given syntax symbols.
	 * @param query The CPQ to parse.
	 * @param labels The label set to use, new labels will <b>not</b> be created if labels are found in the
	 *        input that are not covered by the given list.
	 * @param join The symbol to use for the join/concatenation operation.
	 * @param intersect The symbol to use for the intersection/conjunction operation.
	 * @param inverse The symbol to use for the inverse edge label operation.
	 * @return The parsed CPQ.
	 * @throws IllegalArgumentException When the given string is not a valid CPQ or contains unknown labels.
	 */
	public static CPQ parse(String query, List<Predicate> labels, char join, char intersect, char inverse) throws IllegalArgumentException{
		return parse(query, mapPredicates(labels), join, intersect, inverse);
	}
	
	/**
	 * Parses the given CPQ in string form to a CPQ instance using the given syntax symbols.
	 * @param query The CPQ to parse.
	 * @param labels A map with predicates found so far.
	 * @param join The symbol to use for the join/concatenation operation.
	 * @param intersect The symbol to use for the intersection/conjunction operation.
	 * @param inverse The symbol to use for the inverse edge label operation.
	 * @return The parsed CPQ.
	 * @throws IllegalArgumentException When the given string is not a valid CPQ.
	 */
	private static CPQ parse(String query, Map<String, Predicate> labels, char join, char intersect, char inverse) throws IllegalArgumentException{
		List<String> parts = split(query, join);
		if(parts.size() > 1){
			return CPQ.concat(parts.stream().map(part->{
				return parse(part, labels, join, intersect, inverse);
			}).toList());
		}
		
		parts = split(query, intersect);
		if(parts.size() > 1){
			return CPQ.intersect(parts.stream().map(part->{
				return parse(part, labels, join, intersect, inverse);
			}).toList());
		}
		
		if(query.equals("id")){
			return CPQ.IDENTITY;
		}
		
		if(query.startsWith("(") && query.endsWith(")")){
			return parse(query.substring(1, query.length() - 1), labels, join, intersect, inverse);
		}
		
		if(query.indexOf('(') == -1 && query.indexOf(')') == -1 && query.indexOf(join) == -1 && query.indexOf(intersect) == -1){
			return CPQ.label(parsePredicate(query, labels, inverse));
		}

		throw new IllegalArgumentException("Invalid CPQ.");
	}
}
