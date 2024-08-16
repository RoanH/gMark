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
	public static final char CHAR_CUP = '∪';

	//TODO to abstract syntax tree
}
