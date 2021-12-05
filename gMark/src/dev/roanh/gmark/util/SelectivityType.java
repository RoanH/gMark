package dev.roanh.gmark.util;

import java.util.Objects;

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
	/**
	 * The node type associated with this selectivity type.
	 */
	private final Type type;
	/**
	 * The selectivity class associated with this selectivity type.
	 */
	private final SelectivityClass selectivity;
	
	/**
	 * Constructs a new selectivity type with the
	 * given type and selectivity class.
	 * @param type The type for this selectivity type.
	 * @param sel The selectivity class for this selectivity type.
	 */
	public SelectivityType(Type type, SelectivityClass sel){
		this.type = type;
		selectivity = sel;
	}
	
	/**
	 * Gets the node type for this selectivity type.
	 * @return The node type for this selectivity type.
	 */
	public Type getType(){
		return type;
	}
	
	/**
	 * Gets the selectivity class for this selectivity type.
	 * @return The selectivity class for this selectivity type.
	 */
	public SelectivityClass getSelectivity(){
		return selectivity;
	}
	
	public int getTypeID(){
		return type.getID();
	}
	
	public static final SelectivityType of(Type type, SelectivityClass sel){
		return new SelectivityType(type, sel);
	}
	
	@Override
	public boolean equals(Object other){
		if(other instanceof SelectivityType){
			SelectivityType selType = (SelectivityType)other;
			return selType.selectivity == selectivity && selType.type.equals(type);
		}else{
			return false;
		}
	}
	
	@Override
	public int hashCode(){
		return Objects.hash(selectivity, type);
	}
	
	@Override
	public String toString(){
		return "(" + type.getAlias() + "," + selectivity + ")";
	}
}