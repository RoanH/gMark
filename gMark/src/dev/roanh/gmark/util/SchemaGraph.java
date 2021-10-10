package dev.roanh.gmark.util;

import java.util.Map;

import dev.roanh.gmark.core.SelectivityClass;
import dev.roanh.gmark.core.graph.Edge;
import dev.roanh.gmark.core.graph.Predicate;
import dev.roanh.gmark.core.graph.Schema;
import dev.roanh.gmark.core.graph.Type;

public class SchemaGraph{
	private RangeList<Map<SelectivityClass, SchemaGraphTripple>> transitions;

	public SchemaGraph(Schema schema){
		//transitions = new RangeList<Map<SelectivityClass, Integer>>(schema.getTypeCount(), ()->new EnumMap<SelectivityClass, Integer>(SelectivityClass.class));
		
		for(Edge edge : schema.getEdges()){
			SelectivityClass sel2 = edge.getSelectivty();
			for(SelectivityClass sel1 : SelectivityClass.values()){//TODO why do we iterate all selectivity values?
				transitions.get(edge.getSourceType()).put(sel1, new SchemaGraphTripple(edge.getPredicate(), edge.getTargetType(), sel1.conjunction(sel2)));
				transitions.get(edge.getTargetType()).put(sel1, new SchemaGraphTripple(edge.getPredicate().getInverse(), edge.getTargetType(), sel1.conjunction(sel2.negate())));
			}
		}
		
		
		
	    //vector<map<SELECTIVITY::type, set<pair<long,pair<size_t,SELECTIVITY::type>>>>> transitions;

		
		
		//list of maps from
		//        SelectivityType->Set of <long, size_t, SelectivityType>
		//list indexed by node
		
		
		
		
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
