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
	/**
	 * The queries stored in this query set.
	 */
	private final List<Query> queries;
	/**
	 * The total time it took to generate the
	 * queries in this query set (in milliseconds).
	 */
	private final long generationTime;
	
	/**
	 * Constructs a new query set with the given
	 * queries and generation time.
	 * @param queries The queries for this query set.
	 * @param time The time in milliseconds it took
	 *        to generate the queries in this query set.
	 */
	public QuerySet(List<Query> queries, long time){
		this.queries = queries;
		generationTime = time;
	}
	
	/**
	 * Gets the queries in this query set.
	 * @return The queries in this query set.
	 */
	public List<Query> getQueries(){
		return queries;
	}
	
	/**
	 * Gets the number of queries in this query set.
	 * @return The number of queries in this query set.
	 */
	public int size(){
		return queries.size();
	}
	
	/**
	 * Gets the query at the given index from this set.
	 * @param index The index of the query to get.
	 * @return The query at the given index.
	 */
	public Query get(int index){
		return queries.get(index);
	}
	
	/**
	 * Gets the arity of the query with the lowest arity in this set.
	 * @return The arity of the query with the lowest arity.
	 */
	public int getMinArity(){
		return queries.stream().mapToInt(Query::getArity).min().orElse(0);
	}
	
	/**
	 * Gets the arity of the query with the highest arity in this set.
	 * @return The arity of the query with the highest arity.
	 */
	public int getMaxArity(){
		return queries.stream().mapToInt(Query::getArity).max().orElse(0);
	}
	
	/**
	 * Gets the number of binary queries in this set (queries with arity 0).
	 * @return The number of binary queries.
	 */
	public int getBinaryQueryCount(){
		return (int)queries.stream().filter(Query::isBinary).count();
	}
	
	/**
	 * Gets the number of conjuncts in the query with the least conjuncts in this set.
	 * @return The number of conjuncts in the query with the least conjuncts.
	 */
	public int getMinConjuncts(){
		return queries.stream().mapToInt(Query::getMinConjuncts).min().orElse(0);
	}
	
	/**
	 * Gets the number of conjuncts in the query with the most conjuncts in this set.
	 * @return The number of conjuncts in the query with the most conjuncts.
	 */
	public int getMaxConjuncts(){
		return queries.stream().mapToInt(Query::getMaxConjuncts).max().orElse(0);
	}
	
	/**
	 * @deprecated Not yet implemented.
	 * @return Always 0.
	 */
	@Deprecated
	public double getStarFraction(){
		//TODO implement
		return 0.0D;
	}
	
	/**
	 * Gets the total time it took to generate the queries in this set.
	 * @return The total generation time in milliseconds.
	 */
	public long getGenerationTime(){
		return generationTime;
	}
	
	/**
	 * Gets the total number of queries in this set with the given shape.
	 * @param shape The query shape to search for.
	 * @return The number of queries with the given shape.
	 * @see QueryShape
	 */
	public int getShapeTotal(QueryShape shape){
		return (int)queries.stream().filter(q->q.hasShape(shape)).count();
	}
	
	/**
	 * Gets the total number of queries in this set with the given selectivity.
	 * @param selectivity The query selectivity to search for.
	 * @return The number of queries with the given selectivity.
	 * @see Selectivity
	 */
	public int getSelectivityTotal(Selectivity selectivity){
		return (int)queries.stream().filter(q->q.hasSelectivity(selectivity)).count();
	}
	
	/**
	 * Gets the fraction of all queries in this set with the given shape.
	 * @param shape The query shape to search for.
	 * @return The fraction of all queries with the given shape.
	 */
	public double getShapeFraction(QueryShape shape){
		return (double)getShapeTotal(shape) / (double)queries.size();
	}
	
	/**
	 * Gets the fraction of all queries in this set with the given selectivity.
	 * @param selectivity The query selectivity to search for.
	 * @return The fraction of all queries with the given selectivity.
	 */
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
