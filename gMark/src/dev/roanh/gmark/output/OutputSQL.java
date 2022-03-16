package dev.roanh.gmark.output;

/**
 * Interface for components that can be converted
 * to an SQL query representing them.
 * @author Roan
 */
public abstract interface OutputSQL{

	//TODO indent writer
	/**
	 * Converts this object to an SQL query.
	 * @return An SQL query representing this object.
	 */
	public abstract String toSQL();
}
