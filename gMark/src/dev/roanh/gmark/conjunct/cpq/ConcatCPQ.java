package dev.roanh.gmark.conjunct.cpq;

import java.util.List;
import java.util.StringJoiner;

public class ConcatCPQ implements CPQ{
	private List<CPQ> cpq;
	
	public ConcatCPQ(List<CPQ> cpq){
		this.cpq = cpq;
	}
	
	@Override
	public String toString(){
		StringJoiner builder = new StringJoiner("◦", "(", ")");
		for(CPQ item : cpq){
			builder.add(item.toString());
		}
		return builder.toString();
	}

	@Override
	public String toSQL(){
		if(cpq.size() == 1){
			return cpq.get(0).toSQL();
		}
		
		StringBuilder buffer = new StringBuilder();
		int n = cpq.size();
		
		buffer.append("(SELECT s0.src AS src, s");
		buffer.append(n - 1);
		buffer.append(".trg AS trg FROM ");
		for(int i = 0; i < n; i++){
			buffer.append(cpq.get(i).toSQL());
			buffer.append(" AS s");
			buffer.append(i);
			if(i < n - 1){
				buffer.append(", ");
			}
		}
		
		buffer.append(" WHERE ");
		for(int i = 0; i < n - 1; i++){
			buffer.append("s");
			buffer.append(i);
			buffer.append(".trg = s");
			buffer.append(i + 1);
			buffer.append(".src");
			if(i < n - 2){
				buffer.append(" AND ");
			}
		}
		
		buffer.append(")");
		return buffer.toString();
	}
}
