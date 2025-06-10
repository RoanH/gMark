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
package dev.roanh.gmark.lang;

import dev.roanh.gmark.ast.QueryFragment;
import dev.roanh.gmark.output.OutputFormal;
import dev.roanh.gmark.output.OutputSQL;
import dev.roanh.gmark.output.OutputXML;

/**
 * Base interface for query language specifications.
 * @author Roan
 */
public abstract interface QueryLanguageSyntax extends OutputSQL, OutputFormal, OutputXML, QueryFragment{
	/**
	 * The character used to denote the intersection/conjunction operator.
	 */
	public static final char CHAR_INTERSECTION = '∩';
	/**
	 * The character used to denote the join/concatenation operator.
	 */
	public static final char CHAR_JOIN = '◦';
	/**
	 * The character used to denote the disjunction operator.
	 */
	public static final char CHAR_DISJUNCTION = '∪';
	/**
	 * The character used to denote the kleene/transitive closure operator.
	 */
	public static final char CHAR_KLEENE = '*';
	/**
	 * The character used to denote negated predicates/labels.
	 */
	public static final char CHAR_INVERSE = '⁻';
	/**
	 * The character used to denote query output assignment.
	 */
	public static final char CHAR_ASSIGN = '←';
	
	/**
	 * Gets the concrete query language used to defined this query.
	 * @return The query language for this query.
	 */
	public abstract QueryLanguage getQueryLanguage();
}
