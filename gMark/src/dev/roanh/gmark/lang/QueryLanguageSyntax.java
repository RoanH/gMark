package dev.roanh.gmark.lang;

import dev.roanh.gmark.ast.QueryFragment;
import dev.roanh.gmark.output.OutputSQL;
import dev.roanh.gmark.output.OutputXML;

/**
 * Base interface for query language specifications.
 * @author Roan
 * @param <SELF> The concrete query language specification.
 */
public abstract interface QueryLanguageSyntax<SELF extends QueryLanguageSyntax<SELF>> extends OutputSQL, OutputXML, QueryFragment<SELF>{
	/**
	 * The character used to denote the intersection/conjunction operator.
	 */
	public static final char CHAR_CAP = '∩';
	/**
	 * The character used to denote the join/concatenation operator.
	 */
	public static final char CHAR_JOIN = '◦';
	/**
	 * The character used to denote the disjunction operator.
	 */
	public static final char CHAR_CUP = '∪';
	/**
	 * The character used to denote the kleene/transitive closure operator.
	 */
	public static final char CHAR_KLEENE = '*';
	/**
	 * The character used to denote negated predicates/labels.
	 */
	public static final char CHAR_INVERSE = '⁻';
	
	public abstract QueryLanguage getQueryLanguage();
}
