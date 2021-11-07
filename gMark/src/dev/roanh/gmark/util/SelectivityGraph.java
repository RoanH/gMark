package dev.roanh.gmark.util;

import java.util.Map;
import java.util.Map.Entry;

import dev.roanh.gmark.core.SelectivityClass;
import dev.roanh.gmark.core.graph.Edge;
import dev.roanh.gmark.core.graph.Schema;
import dev.roanh.gmark.core.graph.Type;

public class SelectivityGraph extends Graph<SelectivityType, Void>{
	//TODO, move to constructor if not used anywhere else
	private RangeList<Map<SelectivityClass, GraphNode<SelectivityType, Void>>> index;
	
	
	public SelectivityGraph(Schema schema, int maxLength){
		
		//compute distance between types (matrix)
		//compute graph from matrix
		
		DistanceMatrix matrix = computeDistanceMatrix(schema);
	
		//TODO in the original codebase this creates links (t1,s1) --s2-> (t2,s1*s2) which would imply this graph is labelled?
		for(Type t1 : schema.getTypes()){
			for(Type t2 : schema.getTypes()){
				for(SelectivityClass sel1 : SelectivityClass.values()){
					for(SelectivityClass sel2 : SelectivityClass.values()){
						//if there exists a path of valid length from t1 to t2 with result selectivity class sel2
						if(matrix.get(t1, t2).getOrDefault(sel2, Integer.MAX_VALUE) <= maxLength){
							//add graph edge (t1, sel1) -> (t2, sel1 * sel2)
							resolve(t1, sel1).addUniqueEdgeTo(resolve(t2, sel1.conjunction(sel2)));
						}
					}
				}
			}
		}
	
	}
	
	private GraphNode<SelectivityType, Void> resolve(Type type, SelectivityClass sel){
		return index.get(type).computeIfAbsent(sel, k->addUniqueNode(new SelectivityType(type, sel)));
	}
	
	
	
	
	
	
	
	
	/**
	 * Computes the shortest path between any two nodes in
	 * the schema graph. This is done using a modified version
	 * of the Floyd-Warshall algorithm (all-pairs shortest path).
	 * @param schema The schema to compute the distance matrix for.
	 * @return The distance matrix for the given graph schema.
	 */
	private static DistanceMatrix computeDistanceMatrix(Schema schema){
		int size = schema.getTypeCount();
		int logSize = (int)Math.log(size) + 4;//TODO why does this use a natural log?
		
		DistanceMatrix matrix = new DistanceMatrix(size);
		DistanceMatrix tmp = new DistanceMatrix(size);
		
		//add all length 1 paths
		for(Edge edge : schema.getEdges()){
			SelectivityClass sel = edge.getSelectivty();
			matrix.get(edge.getSourceType(), edge.getTargetType()).put(sel, 1);
			matrix.get(edge.getTargetType(), edge.getSourceType()).put(sel.negate(), 1);
		}
		
		for(int step = 0; step < logSize; step++){
			//make a copy of the matrix
			tmp.overwrite(matrix);
			
			//for each source type
			for(int i = 0; i < size; i++){
				//for each target type
				for(int j = 0; j < size; j++){
					//for pairs of connected edges
					for(int k = 0; k < size; k++){
						for(Entry<SelectivityClass, Integer> first : tmp.get(i, k).entrySet()){
							for(Entry<SelectivityClass, Integer> second : tmp.get(k, j).entrySet()){
								//update length to be the minimum of an existing length and the current path
								matrix.get(i, j).merge(
									first.getKey().conjunction(second.getKey()),//result of following these two edges after each other
									first.getValue() + second.getValue(),
									Math::min
								);
							}
						}
					}
				}
			}
		}
		
		return matrix;
	}
	
	private static final class DistanceMatrix extends RangeMatrix<Map<SelectivityClass, Integer>>{
		
		private DistanceMatrix(int size){
			super(size, Util.selectivityMapSupplier());
		}
		
		private void overwrite(DistanceMatrix data){
			for(int i = 0; i < getSize(); i++){
				for(int j = 0; j < getSize(); j++){
					Map<SelectivityClass, Integer> cell = get(i, j);
					Map<SelectivityClass, Integer> other = data.get(i, j);
					for(SelectivityClass sel : SelectivityClass.values()){
						Integer value = other.get(sel);
						if(value == null){
							cell.remove(sel);
						}else{
							cell.put(sel, value);
						}
					}
				}
			}
		}
	}
}
