package nl.group9.quicksilver.core.data;

/**
 * Cardinality statistics for a query.
 * @author Roan
 * @param noOut The number of distinct possible source nodes for the query, these are all
 *        potential start point nodes for a query. This will be 1 by definition
 *        if a query has a bound source node.
 * @param noPaths The total number of distinct paths between the source and target nodes.
 * @param noIn The number of distinct possible target nodes for the query, these are all
 *        potential end point nodes for a query. This will be 1 by definition
 *        if a query has a bound target node.
 */
public record CardStat(int noOut, int noPaths, int noIn){
}
