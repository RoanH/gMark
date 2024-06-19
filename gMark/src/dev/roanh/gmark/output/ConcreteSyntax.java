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
package dev.roanh.gmark.output;

import java.util.Locale;
import java.util.function.Function;

import dev.roanh.gmark.query.Query;

/**
 * Enum defining concrete output syntaxes for generated queries.
 * @author Roan
 */
public enum ConcreteSyntax{
	/**
	 * SQL query output.
	 */
	SQL("sql", "sql", OutputSQL::toSQL);
	
	/**
	 * The ID of this concrete syntax as used for command
	 * line arguments and output folder creation.
	 */
	private final String id;
	/**
	 * The file extension of files created for this syntax.
	 */
	private final String extension;
	/**
	 * Function to convert a query to this concrete syntax.
	 */
	private final Function<Query, String> convert;
	
	/**
	 * Constructs a new concrete syntax with the given ID,
	 * file extension and query conversion function.
	 * @param id The ID of this concrete syntax.
	 * @param extension The file extension for this syntax.
	 * @param convert The function to convert queries to this syntax.
	 */
	private ConcreteSyntax(String id, String extension, Function<Query, String> convert){
		this.id = id;
		this.extension = extension;
		this.convert = convert;
	}
	
	/**
	 * Gets the display name of this syntax.
	 * @return The display name of this syntax.
	 */
	public String getName(){
		return id.toUpperCase(Locale.ROOT);
	}
	
	/**
	 * The ID of this syntax as used for command line
	 * arguments and file output.
	 * @return The ID of this syntax.
	 */
	public String getID(){
		return id;
	}
	
	/**
	 * Gets the file extension for output files created
	 * for this concrete syntax.
	 * @return The file extension.
	 */
	public String getExtension(){
		return extension;
	}
	
	/**
	 * Converts the given query to this syntax.
	 * @param query The query to convert.
	 * @return The query represented in this concrete syntax.
	 */
	public String convert(Query query){
		return convert.apply(query);
	}
	
	/**
	 * Resolves a concrete syntax by its identifier name.
	 * @param id The ID of the syntax to resolve.
	 * @return The resolved syntax or <code>null</code> if
	 *         no syntax with the given ID was found.
	 */
	public static ConcreteSyntax fromName(String id){
		for(ConcreteSyntax syntax : values()){
			if(syntax.id.equalsIgnoreCase(id)){
				return syntax;
			}
		}
		
		return null;
	}
}
