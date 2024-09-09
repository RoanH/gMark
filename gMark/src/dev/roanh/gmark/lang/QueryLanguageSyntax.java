package dev.roanh.gmark.lang;

import dev.roanh.gmark.ast.QueryFragment;
import dev.roanh.gmark.output.OutputSQL;
import dev.roanh.gmark.output.OutputXML;

/**
 * Base interface for query language specifications.
 * @author Roan
 */
public abstract interface QueryLanguageSyntax extends OutputSQL, OutputXML, QueryFragment{
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
	 * Gets the concrete query language used to defined this query.
	 * @return The query language for this query.
	 */
	public abstract QueryLanguage getQueryLanguage();
}
