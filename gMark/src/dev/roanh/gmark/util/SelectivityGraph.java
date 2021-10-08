package dev.roanh.gmark.util;

import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;

import dev.roanh.gmark.core.SelectivityClass;
import dev.roanh.gmark.core.graph.GraphConfig;
import dev.roanh.gmark.core.graph.Schema;

public class SelectivityGraph{

	
	
	public SelectivityGraph(){
		//compute distance between types (matrix)
		//compute graph from matrix
	}
	
	
	
	
	
	
	
	
	
	
	
	private static void computeDistanceMatrix(Schema schema){
		int size = schema.getTypeCount();
		int logSize = (int)Math.log(size) + 4;//TODO why does this use a natural log?
		
		DistanceMatrix matrix = new DistanceMatrix(size);
		DistanceMatrix tmp = new DistanceMatrix(size);
		
		
		
		
		
		
		
		
	}
	
	private static final class DistanceMatrix extends RangeMatrix<Map<SelectivityClass, Integer>>{
		
		private DistanceMatrix(int size){
			super(size, ()->new EnumMap<SelectivityClass, Integer>(SelectivityClass.class));
		}
		
		private void overwrite(DistanceMatrix data){
			for(int i = 0; i < size; i++){
				for(int j = 0; j < size; j++){
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
