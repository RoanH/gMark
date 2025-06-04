package dev.roanh.gmark.lang.cq;

import dev.roanh.gmark.ast.EdgeAtom;
import dev.roanh.gmark.ast.OperationType;
import dev.roanh.gmark.ast.QueryFragment;
import dev.roanh.gmark.ast.QueryTree;
import dev.roanh.gmark.type.schema.Predicate;

public class AtomCQ implements QueryFragment, EdgeAtom{
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
	public OperationType getOperationType(){
		return OperationType.EDGE;
	}

	@Override
	public QueryTree toAbstractSyntaxTree(){
		return QueryTree.ofAtom(this);
	}
}
