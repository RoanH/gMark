package dev.roanh.gmark.query;

import java.util.List;

import dev.roanh.gmark.core.QueryShape;
import dev.roanh.gmark.core.Selectivity;
import dev.roanh.gmark.output.OutputXML;
import dev.roanh.gmark.util.IndentWriter;

/**
 * Represents a collection of several generated queries
 * together with an easy way to get information about
 * the whole set of generated queries.
 * @author Roan
 */
public class QuerySet implements OutputXML{
	private List<Query> queries;
	private long generationTime;
	
	public QuerySet(List<Query> queries, long time){
		this.queries = queries;
		generationTime = time;
	}
	
	public List<Query> getQueries(){
		return queries;
	}
	
	public int getSize(){
		return queries.size();
	}
	
	public Query get(int index){
		return queries.get(index);
	}
	
	public int getMinArity(){
		return queries.stream().mapToInt(Query::getArity).min().orElse(0);
	}
	
	public int getMaxArity(){
		return queries.stream().mapToInt(Query::getArity).max().orElse(0);
	}
	
	public int getBinaryQueryCount(){
		return (int)queries.stream().filter(Query::isBinary).count();
	}
	
	public int getMinConjuncts(){
		return queries.stream().mapToInt(Query::getMinConjuncts).min().orElse(0);
	}
	
	public int getMaxConjuncts(){
		return queries.stream().mapToInt(Query::getMaxConjuncts).max().orElse(0);
	}
	
	public double getStarFraction(){
		//TODO how is this computed
		return 0.0D;
	}
	
	public long getGenerationTime(){
		return generationTime;
	}
	
	public int getShapeTotal(QueryShape shape){
		return (int)queries.stream().filter(q->q.hasShape(shape)).count();
	}
	
	public int getSelectivityTotal(Selectivity selectivity){
		return (int)queries.stream().filter(q->q.hasSelectivity(selectivity)).count();
	}
	
	public double getShapeFraction(QueryShape shape){
		return (double)getShapeTotal(shape) / (double)queries.size();
	}
	
	public double getSelectivityFraction(Selectivity selectivity){
		return (double)getSelectivityTotal(selectivity) / (double)queries.size();
	}

	@Override
	public void writeXML(IndentWriter writer){
		writer.println("<queries>", 2);
		queries.forEach(query->query.writeXML(writer));
		writer.println(2, "</queries>");
	}
}
