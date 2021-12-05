package dev.roanh.gmark.query;

public abstract class Conjunct{
	private Variable source;
	private Variable target;
	private boolean star;//TODO rpq conjuncts can have a star... cpq could maybe have them on the conjuncts too?
	
	protected Conjunct(Variable source, Variable target, boolean star){
		this.source = source;
		this.target = target;
		this.star = star;
	}
}
