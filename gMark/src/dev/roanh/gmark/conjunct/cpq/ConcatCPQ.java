package dev.roanh.gmark.conjunct.cpq;

import java.util.List;
import java.util.StringJoiner;

import dev.roanh.gmark.conjunct.cpq.QueryGraphCPQ.Vertex;
import dev.roanh.gmark.util.IndentWriter;

/**
 * CPQ modelling the concatenation of a number of CPQs
 * (also known as the join operation).
 * @author Roan
 */
public class ConcatCPQ implements CPQ{
	/**
	 * In order the concatenated CPQs.
	 */
	private List<CPQ> cpq;
	
	/**
	 * Constructs a new concat CPQ with the
	 * given list of CPQs to concatenate.
	 * @param cpq The CPQs to concatenate.
	 */
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

	@Override
	public void writeXML(IndentWriter writer){
		writer.println("<cpq type=\"concat\">", 2);
		cpq.forEach(c->c.writeXML(writer));
		writer.println(2, "</cpq>");
	}

	@Override
	public QueryGraphCPQ toQueryGraph(Vertex source, Vertex target){
		if(cpq.size() == 1){
			return cpq.get(0).toQueryGraph(source, target);
		}
		
		Vertex mid = new Vertex();
		QueryGraphCPQ chain = cpq.get(0).toQueryGraph(source, mid);
		for(int i = 1; i < cpq.size() - 1; i++){
			Vertex to = i == cpq.size() - 1 ? target : new Vertex();
			chain.union(cpq.get(i).toQueryGraph(mid, to));
			mid = to;
		}
		
		chain.setTarget(target);
		return chain;
	}
}
