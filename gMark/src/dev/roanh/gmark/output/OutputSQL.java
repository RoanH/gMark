package dev.roanh.gmark.output;

import dev.roanh.gmark.util.IndentWriter;

/**
 * Interface for components that can be converted
 * to an SQL query representing them.
 * @author Roan
 */
public abstract interface OutputSQL{
	
	public abstract void writeSQL(IndentWriter writer);

	//TODO indent writer
	/**
	 * Converts this object to an SQL query.
	 * @return An SQL query representing this object.
	 */
	public abstract String toSQLi();
}
