package dev.roanh.gmark.util;

import java.util.Map;

import dev.roanh.gmark.core.SelectivityClass;
import dev.roanh.gmark.core.graph.Edge;
import dev.roanh.gmark.core.graph.Predicate;
import dev.roanh.gmark.core.graph.Schema;
import dev.roanh.gmark.core.graph.Type;

/**
 * The schema graph is a graph that has selectivity types
 * as its nodes. These nodes represent the combination of
 * some node type and some selectivity class. There are
 * directed labelled edges present between these selectivity
 * type nodes. These edges indicate that it is possible to
 * extend a path query with an edge with a certain label to
 * end up at the target selectivity type.
 * @author Roan
 * @see SelectivityType
 * @see Predicate
 */
public class SchemaGraph extends Graph<SelectivityType, Predicate>{
	//TODO, move to constructor if not used anywhere else
	private RangeList<Map<SelectivityClass, GraphNode<SelectivityType, Predicate>>> index;

	/**
	 * Constructs a new schema graph from the given schema.
	 * @param schema The graph schema to build the schema
	 *        graph from.
	 */
	public SchemaGraph(Schema schema){
		index = new RangeList<Map<SelectivityClass, GraphNode<SelectivityType, Predicate>>>(schema.getTypeCount(), Util.selectivityMapSupplier());
		
		for(Edge edge : schema.getEdges()){
			SelectivityClass sel2 = edge.getSelectivty();
			for(SelectivityClass sel1 : SelectivityClass.values()){
				//TODO remove debug prints
				System.out.println(edge.getSourceType().getID() + "," + sel1 + " via " + edge.getPredicate().getID() + " to " + edge.getTargetType().getID() + "," + sel1.conjunction(sel2));
				resolve(edge.getSourceType(), sel1).addUniqueEdgeTo(resolve(edge.getTargetType(), sel1.conjunction(sel2)), edge.getPredicate());
				System.out.println(edge.getTargetType().getID() + "," + sel1 + " via " + (-edge.getPredicate().getID() - 1) + " to " + edge.getSourceType().getID() + "," + sel1.conjunction(sel2.negate()));
				resolve(edge.getTargetType(), sel1).addUniqueEdgeTo(resolve(edge.getSourceType(), sel1.conjunction(sel2.negate())), edge.getPredicate().getInverse());
			}
		}
	}
	
	private GraphNode<SelectivityType, Predicate> resolve(Type type, SelectivityClass sel){
		return index.get(type).computeIfAbsent(sel, k->addUniqueNode(new SelectivityType(type, sel)));
	}
	
	/**
	 * Removes all nodes (and associated edges) from the schema graph that are not
	 * reachable when drawing paths. All valid paths should start at a node with a
	 * selectivity class of {@link SelectivityClass#ONE_ONE} or {@link SelectivityClass#EQUALS}.
	 * Any node that is not reachable from a node with one of these selectivity classes
	 * is removed together with all the edges connected to it.
	 */
	public void removeUnreachable(){
		int removed;
		do{
			removed = removeNodeIf(node->{
				SelectivityClass sel = node.getData().getSelectivity();
				if(sel == SelectivityClass.ONE_ONE || sel == SelectivityClass.EQUALS){
					return false;
				}else{
					if(node.getInEdges().isEmpty()){
						return true;
					}else{
						//ensure we remove nodes that only have a self loop
						for(GraphEdge<SelectivityType, Predicate> edge : node.getInEdges()){
							if(!edge.getSourceNode().equals(node)){
								return false;
							}
						}
						return true;
					}
				}
			}).size();
		}while(removed != 0);
	}
	
	/**
	 * Prints all the nodes in the schema graph
	 * to the standard output.
	 */
	public void printNodes(){
		getNodes().forEach(System.out::println);
	}
	
	/**
	 * Prints all the edges in the schema graph
	 * to the standard output.
	 */
	public void printEdges(){
		for(GraphEdge<SelectivityType, Predicate> edge : getEdges()){
			System.out.println(edge.getSource() + " -> " + edge.getData().getAlias() + " -> " + edge.getTarget());
		}
	}
}
