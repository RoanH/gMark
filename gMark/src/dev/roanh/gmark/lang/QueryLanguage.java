package dev.roanh.gmark.lang;

import dev.roanh.gmark.output.OutputSQL;
import dev.roanh.gmark.output.OutputXML;

public abstract interface QueryLanguage extends OutputSQL, OutputXML{
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

	//TODO to abstract syntax tree
	
	//TODO get type? name? maybe rename this interface so the name can reused for an enum?
}
