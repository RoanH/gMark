package dev.roanh.gmark.util;

import java.util.BitSet;

//TODO rename
public class SmartBitSet{
	private final BitSet data;
	private int min = Integer.MAX_VALUE;
	private int max = 0;
	
	public SmartBitSet(int size){
		data = new BitSet(size);
	}
	
	public void rangeSet(int index){
		data.set(index);
		min = Math.min(min, index);
		max = Math.max(max, index);
	}
	
	public boolean get(int index){
		return data.get(index);
	}
	
	public void clear(int from, int to){
		if(to >= from){
			data.clear(from, to + 1);
		}
	}
	
	public void clear(){
		data.clear();
	}
	
	public void rangeClear(){
		clear(min, max);
		min = Integer.MAX_VALUE;
		max = 0;
	}
	
	public int count(){
		return data.cardinality();
	}
	
//	public int rangeCount(){
//		//TODO bitset does not support this
//	}
}
