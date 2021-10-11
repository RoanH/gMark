package dev.roanh.gmark.util;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import dev.roanh.gmark.core.SelectivityClass;
import dev.roanh.gmark.core.graph.Edge;
import dev.roanh.gmark.core.graph.Predicate;
import dev.roanh.gmark.core.graph.Schema;
import dev.roanh.gmark.core.graph.Type;

public class SchemaGraph{
	private RangeList<Map<SelectivityClass, SchemaGraphTripple>> transitions;
	private Schema schema;

	public SchemaGraph(Schema schema){
		this.schema = schema;
		transitions = new RangeList<Map<SelectivityClass, SchemaGraphTripple>>(schema.getTypeCount(), Util.selectivityMapSupplier());
		
		for(Edge edge : schema.getEdges()){
			SelectivityClass sel2 = edge.getSelectivty();
			for(SelectivityClass sel1 : SelectivityClass.values()){
				transitions.get(edge.getSourceType()).put(sel1, new SchemaGraphTripple(edge.getPredicate(), edge.getTargetType(), sel1.conjunction(sel2)));
				transitions.get(edge.getTargetType()).put(sel1, new SchemaGraphTripple(edge.getPredicate().getInverse(), edge.getTargetType(), sel1.conjunction(sel2.negate())));
			}
		}
	}
	
	public void printNodes(){
		List<Type> types = schema.getTypes();
		for(int i = 0; i < transitions.size(); i++){
			for(SelectivityClass cl : transitions.get(i).keySet()){
				System.out.println(types.get(i).getID() + "," + cl);
			}
		}
	}
	
	public void printEdges(){
		
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
	}
}
