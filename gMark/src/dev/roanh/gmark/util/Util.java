package dev.roanh.gmark.util;

import java.util.Collection;
import java.util.Random;

public class Util{

	public static <T> T selectRandom(Random random, Collection<T> data){
		if(!data.isEmpty()){
			int idx = random.nextInt(data.size());
			for(T item : data){
				if(idx-- == 0){
					return item;
				}
			}
		}
		return null;
	}
	
	//min and max inclusive
	public static int uniformRandom(Random random, int min, int max){
		return min + random.nextInt(max - min + 1);
	}
}
