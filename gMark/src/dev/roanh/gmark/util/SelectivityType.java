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
	private Type type;
	private SelectivityClass selectivity;
	
	public SelectivityType(Type type, SelectivityClass sel){
		this.type = type;
		selectivity = sel;
	}
	
	public Type getType(){
		return type;
	}
	
	public SelectivityClass getSelectivity(){
		return selectivity;
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
}