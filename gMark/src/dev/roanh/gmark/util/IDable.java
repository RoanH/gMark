package dev.roanh.gmark.util;

/**
 * Interface to represent objects that are
 * associated with some unique ID.
 * @author Roan
 */
public abstract interface IDable{
	
	/**
	 * Gets the unique ID for this object. This
	 * ID uniquely identifies all objects of this type.
	 * @return The unique ID for this object.
	 */
	public abstract int getID();
}
