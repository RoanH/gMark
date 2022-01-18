package dev.roanh.gmark.conjunct.cpq;

import dev.roanh.gmark.query.Conjunct;

public class ConjunctCPQ extends Conjunct{
	private CPQ cpq;
	
	public ConjunctCPQ(CPQ cpq){
		this.cpq = cpq;
	}

	@Override
	protected String getInnerString(){
		return cpq.toString();
	}

	@Override
	public String toSQL(){
		if(hasStar()){
			return "(SELECT edge.src, edge.src FROM edge UNION SELECT edge.trg, edge.trg FROM edge UNION " + cpq.toSQL() + ")";
		}else{
			return cpq.toSQL();
		}
	}
}
