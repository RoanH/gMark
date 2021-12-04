package dev.roanh.gmark.util;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;

import dev.roanh.gmark.core.SelectivityClass;

public class Util{
	private static final ThreadLocal<Random> random = ThreadLocal.withInitial(Random::new);
	
	public static Random getRandom(){
		return random.get();
	}
	
	//thread local
	public static void setRandomSeed(long seed){
		random.get().setSeed(seed);
	}

	public static <T> T selectRandom(Collection<T> data){
		if(!data.isEmpty()){
			int idx = getRandom().nextInt(data.size());
			for(T item : data){
				if(idx-- == 0){
					return item;
				}
			}
		}
		return null;
	}
	
	//min and max inclusive
	public static int uniformRandom(int min, int max){
		return min + getRandom().nextInt(max - min + 1);
	}
	
	public static <T> Supplier<Map<SelectivityClass, T>> selectivityMapSupplier(){
		return ()->new EnumMap<SelectivityClass, T>(SelectivityClass.class);
	}
	
	public static <T, R> R applyOrNull(T data, Function<T, R> function){
		return data == null ? null : function.apply(data);
	}
}
