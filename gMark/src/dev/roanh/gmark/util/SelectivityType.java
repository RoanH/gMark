package dev.roanh.gmark.util;

import dev.roanh.gmark.core.SelectivityClass;
import dev.roanh.gmark.core.graph.Type;

/**
 * Selectivity types are a combination of a {@link Type node type}
 * and {@link SelectivityClass selectivity class}. They are the
 * nodes in both the {@link SchemaGraph schema graph} and
 * {@link SelectivityGraph selectivity graph}.
 * @author Roan
 * @see Type
 * @see SelectivityClass
 * @see SelectivityGraph
 * @see SchemaGraph
 */
public class SelectivityType{
	private Type type;
	private SelectivityClass selectivity;
	
	public SelectivityType(Type type, SelectivityClass sel){
		this.type = type;
		selectivity = sel;
	}
}
