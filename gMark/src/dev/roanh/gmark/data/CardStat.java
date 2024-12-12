package dev.roanh.gmark.data;

import dev.roanh.gmark.eval.PathQuery;

/**
 * Cardinality statistics for a reachability path query.
 * @author Roan
 * @param sources The number of distinct possible source nodes for the query, these are all
 *        potential start point nodes for a query. This will be 1 by definition
 *        if a query has a bound source node (unless the query matches nothing).
 * @param paths The total number of distinct paths between the source and target nodes.
 * @param targets The number of distinct possible target nodes for the query, these are all
 *        potential end point nodes for a query. This will be 1 by definition
 *        if a query has a bound target node (unless the query matches nothing).
 * @see PathQuery
 */
public record CardStat(int sources, int paths, int targets){
}
