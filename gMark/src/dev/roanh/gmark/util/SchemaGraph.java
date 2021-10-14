package dev.roanh.gmark.util;

import java.util.Map;
import java.util.Objects;

import dev.roanh.gmark.core.SelectivityClass;
import dev.roanh.gmark.core.graph.Edge;
import dev.roanh.gmark.core.graph.Predicate;
import dev.roanh.gmark.core.graph.Schema;
import dev.roanh.gmark.core.graph.Type;

public class SchemaGraph extends Graph<SelectivityType, Predicate>{
	private RangeList<Map<SelectivityClass, GraphNode<SelectivityType, Predicate>>> index;

	public SchemaGraph(Schema schema){
		index = new RangeList<Map<SelectivityClass, GraphNode<SelectivityType, Predicate>>>(schema.getTypeCount() * SelectivityClass.values().length, Util.selectivityMapSupplier());
		
		for(Edge edge : schema.getEdges()){
			SelectivityClass sel2 = edge.getSelectivty();
			for(SelectivityClass sel1 : SelectivityClass.values()){
				resolve(edge.getSourceType(), sel1).addUniqueEdgeTo(resolve(edge.getTargetType(), sel1.conjunction(sel2)), edge.getPredicate());
				resolve(edge.getTargetType(), sel1).addUniqueEdgeTo(resolve(edge.getTargetType(), sel1.conjunction(sel2.negate())), edge.getPredicate().getInverse());
			}
		}
	}
	
	private GraphNode<SelectivityType, Predicate> resolve(Type type, SelectivityClass sel){
		return index.get(type).computeIfAbsent(sel, k->addUniqueNode(new SelectivityType(type, sel)));
	}
	
	public void printNodes(){
		getNodes().forEach(System.out::println);
	}
	
	public void printEdges(){
		for(GraphEdge<SelectivityType, Predicate> edge : getEdges()){
			System.out.println(edge.getSource() + " -> " + edge.getData().getAlias() + " -> " + edge.getTarget());
		}
	}
	
	public static final class SchemaGraphTripple{
		private Predicate predicate;
		private Type target;
		private SelectivityClass selectivity;
		
		private SchemaGraphTripple(Predicate predicate, Type target, SelectivityClass selectivity){
			this.predicate = predicate;
			this.target = target;
			this.selectivity = selectivity;
		}
		
		@Override
		public boolean equals(Object other){
			if(other instanceof SchemaGraphTripple){
				SchemaGraphTripple trip = (SchemaGraphTripple)other;
				return trip.selectivity == selectivity && predicate.equals(trip.predicate) && target.equals(trip.target);
			}else{
				return false;
			}
		}
		
		@Override
		public int hashCode(){
			return Objects.hash(predicate, target, selectivity);
		}
	}
}
