package dev.roanh.gmark.util;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import dev.roanh.gmark.core.SelectivityClass;
import dev.roanh.gmark.core.graph.Edge;
import dev.roanh.gmark.core.graph.Predicate;
import dev.roanh.gmark.core.graph.Schema;
import dev.roanh.gmark.core.graph.Type;

public class SchemaGraph extends Graph<SelectivityType, Predicate>{
	private RangeList<Map<SelectivityClass, GraphNode<SelectivityType, Predicate>>> index;
	@Deprecated
	private RangeList<Map<SelectivityClass, Set<SchemaGraphTripple>>> transitions;
	private Schema schema;

	public SchemaGraph(Schema schema){
		this.schema = schema;
		transitions = new RangeList<Map<SelectivityClass, Set<SchemaGraphTripple>>>(schema.getTypeCount(), Util.selectivityMapSupplier());
		index = new RangeList<Map<SelectivityClass, GraphNode<SelectivityType, Predicate>>>(schema.getTypeCount() * SelectivityClass.values().length, Util.selectivityMapSupplier());
		
		for(Edge edge : schema.getEdges()){
			SelectivityClass sel2 = edge.getSelectivty();
			for(SelectivityClass sel1 : SelectivityClass.values()){
				transitions.get(edge.getSourceType()).computeIfAbsent(sel1, k->{
					return new HashSet<SchemaGraphTripple>();
				}).add(new SchemaGraphTripple(edge.getPredicate(), edge.getTargetType(), sel1.conjunction(sel2)));
				transitions.get(edge.getTargetType()).computeIfAbsent(sel1, k->{
					return new HashSet<SchemaGraphTripple>();
				}).add(new SchemaGraphTripple(edge.getPredicate().getInverse(), edge.getTargetType(), sel1.conjunction(sel2.negate())));
				
				resolve(edge.getSourceType(), sel1).addUniqueEdgeTo(resolve(edge.getTargetType(), sel1.conjunction(sel2)), edge.getPredicate());
				resolve(edge.getTargetType(), sel1).addUniqueEdgeTo(resolve(edge.getTargetType(), sel1.conjunction(sel2.negate())), edge.getPredicate().getInverse());
			}
		}
	}
	
	private GraphNode<SelectivityType, Predicate> resolve(Type type, SelectivityClass sel){
		return index.get(type).computeIfAbsent(sel, k->addUniqueNode(new SelectivityType(type, sel)));
	}
	
//	public Set<SchemaGraphTripple> getOutEdges(Type type, SelectivityClass selectivity){
//		return transitions.get(type).get(selectivity);
//	}
	
	public void printNodes(){
		List<Type> types = schema.getTypes();
		for(int i = 0; i < transitions.size(); i++){
			for(SelectivityClass cl : transitions.get(i).keySet()){
				System.out.println("(" + types.get(i).getAlias() + "," + cl + ")");
			}
		}
	}
	
	public void printEdges(){
		List<Type> types = schema.getTypes();
		for(int i = 0; i < transitions.size(); i++){
			for(Entry<SelectivityClass, Set<SchemaGraphTripple>> entry : transitions.get(i).entrySet()){
				for(SchemaGraphTripple trip : entry.getValue()){
					System.out.println("(" + types.get(i).getAlias() + "," + entry.getKey() + ") -> " + trip.predicate.getAlias() + " -> (" + trip.target.getAlias() + "," + trip.selectivity + ")");
				}
			}
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
