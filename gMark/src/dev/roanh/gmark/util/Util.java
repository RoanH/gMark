package dev.roanh.gmark.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;

import dev.roanh.gmark.core.SelectivityClass;

/**
 * Class providing various small utilities as well
 * as thread bound random operations.
 * @author Roan
 */
public class Util{
	/**
	 * Random instances for each thread.
	 */
	private static final ThreadLocal<Random> random = ThreadLocal.withInitial(Random::new);
	
	/**
	 * Gets a random instance bound to the current thread.
	 * @return A thread local random instance.
	 */
	public static Random getRandom(){
		return random.get();
	}
	
	/**
	 * Sets the seed of the random instance for the current thread.
	 * @param seed The new seed.
	 * @see #getRandom()
	 */
	public static void setRandomSeed(long seed){
		random.get().setSeed(seed);
	}

	/**
	 * Randomly selects an element from the given collection.
	 * @param <T> The element data type.
	 * @param data The collection to pick an element from.
	 * @return The selected element or <code>null</code>
	 *         when the provided collection was empty.
	 */
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
	
	/**
	 * Generates a random integer between the given
	 * minimum and maximum value (both inclusive).
	 * @param min The minimum value.
	 * @param max The maximum value.
	 * @return The randomly generated value.
	 */
	public static int uniformRandom(int min, int max){
		return min + getRandom().nextInt(max - min + 1);
	}
	
	/**
	 * Returns a {@link Supplier} that constructs a new enum map
	 * from the {@link SelectivityClass} enum to the given data type.
	 * @param <T> The data type to map to.
	 * @return A supplier that returns a map that maps from
	 *         selectivity classes to the given data type.
	 * @see Supplier
	 * @see SelectivityClass
	 */
	public static <T> Supplier<Map<SelectivityClass, T>> selectivityMapSupplier(){
		return ()->new EnumMap<SelectivityClass, T>(SelectivityClass.class);
	}
	
	/**
	 * Applies the given function to the given data, unless the
	 * data is <code>null</code> then <code>null</code> is returned
	 * instead.
	 * @param <T> The input data type.
	 * @param <R> The output data type.
	 * @param data The data to give to the function.
	 * @param function The function to run on the data.
	 * @return The result of applying the given function to the given
	 *         data or <code>null</code> when the given data is <code>null</code>.
	 */
	public static <T, R> R applyOrNull(T data, Function<T, R> function){
		return data == null ? null : function.apply(data);
	}
	
	/**
	 * Checks if the given folder is empty or not.
	 * @param folder The folder to check.
	 * @return True if the given folder does not
	 *         contain any files or folders.
	 * @throws IOException When an IOException occurs.
	 */
	public static boolean isEmpty(Path folder) throws IOException{
		return !Files.walk(folder).filter(path->!path.equals(folder)).findFirst().isPresent();
	}
}
