package dev.roanh.gmark.query;

public abstract class Conjunct{
	private Variable source;
	private Variable target;
	private boolean star;
	
//	protected Conjunct(Variable source, Variable target, boolean star){
//		this.source = source;
//		this.target = target;
//		this.star = star;
//	}
	
	//TODO this relies on the constructing party to set the data, not very nice, use a builder?
	public void setData(Variable source, Variable target, boolean star){
		this.source = source;
		this.target = target;
		this.star = star;
	}
	
	public boolean hasStar(){
		return star;
	}
	
	//TODO should probably not be a thing
	protected abstract String getInnerString();
	
	//does not have to respect star
	protected abstract String toPartialSQL();
	
	@Override
	public String toString(){
		return "(" + source + "," + getInnerString() + "," + target + ")";
	}
}
