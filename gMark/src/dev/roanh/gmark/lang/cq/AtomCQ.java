package dev.roanh.gmark.lang.cq;

import java.util.Objects;

import dev.roanh.gmark.ast.EdgeAtom;
import dev.roanh.gmark.type.schema.Predicate;

public class AtomCQ implements EdgeAtom{
	private final VarCQ source;
	private final Predicate label;
	private final VarCQ target;
	
	public AtomCQ(VarCQ source, Predicate label, VarCQ target){
		this.source = source;
		this.label = label;
		this.target = target;
	}
	
	@Override
	public VarCQ getSource(){
		return source;
	}
	
	@Override
	public VarCQ getTarget(){
		return target;
	}
	
	@Override
	public Predicate getLabel(){
		return label;
	}
	
	@Override
	public String toString(){
		return label.getAlias() + "(" + source.getName() + ", " + target.getName() + ")";
	}
	
	@Override
	public boolean equals(Object obj){
		return obj instanceof AtomCQ atom && Objects.equals(atom.source, source) && Objects.equals(atom.label, label) && Objects.equals(atom.target, target);
	}
	
	@Override
	public int hashCode(){
		return Objects.hash(source, label, target);
	}
}
