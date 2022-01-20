package dev.roanh.gmark.conjunct.cpq;

public class IntersectionCPQ implements CPQ{
	private CPQ first;
	private CPQ second;
	
	public IntersectionCPQ(CPQ first, CPQ second){
		this.first = first;
		this.second = second;
		if(first == CPQ.IDENTITY){
			this.first = this.second;
			this.second = CPQ.IDENTITY;
		}
	}
	
	@Override
	public String toString(){
		return "(" + first + " ∩ " + second + ")";
	}

	@Override
	public String toSQL(){
		if(second == CPQ.IDENTITY){
			return "(SELECT ii.src AS src, ii.trg AS trg FROM " + first.toSQL() + " AS ii WHERE ii.src = ii.trg)";
		}else{
			return "(" + first.toSQL() + " INTERSECT " + second.toSQL() + ")";
		}
	}
}
