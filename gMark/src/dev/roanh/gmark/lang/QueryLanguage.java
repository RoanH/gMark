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
