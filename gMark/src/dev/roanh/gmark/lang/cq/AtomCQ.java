package dev.roanh.gmark.lang.cq;

import dev.roanh.gmark.type.schema.Predicate;

public class AtomCQ{
	private final VarCQ source;
	private final Predicate label;
	private final VarCQ target;
	
	public AtomCQ(VarCQ source, Predicate label, VarCQ target){
		this.source = source;
		this.label = label;
		this.target = target;
	}
	
	public VarCQ getSource(){
		return source;
	}
	
	public Predicate getLabel(){
		return label;
	}
	
	public VarCQ getTarget(){
		return target;
	}
	
	@Override
	public String toString(){
		return label.getAlias() + "(" + source.getName() + ", " + target.getName() + ")";
	}
}
