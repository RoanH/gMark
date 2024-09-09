package dev.roanh.gmark.lang;

import dev.roanh.gmark.ast.OperationType;
import dev.roanh.gmark.lang.cpq.CPQ;
import dev.roanh.gmark.lang.rpq.RPQ;

/**
 * Enum of implemented concrete query languages. A query language
 * is a formal syntax that can be used to query a database that is
 * build using a subset of database operations.
 * @author Roan
 * @see OperationType
 */
public enum QueryLanguage{
	/**
	 * The language of Conjunctive Path Queries.
	 * @see CPQ
	 */
	CPQ,
	/**
	 * The language of Regular Path Queries.
	 * @see RPQ
	 */
	RPQ
}
