package dev.roanh.gmark.util;

import java.util.BitSet;

/**
 * Smart bit set that remember which part of the entire bit set
 * was actually used to improve reset performance.
 * @author Roan
 */
public class RangeBitSet{
	/**
	 * The underlying bit set.
	 */
	private final BitSet data;
	/**
	 * The lowest bit set index that was written to.
	 */
	private int min = Integer.MAX_VALUE;
	/**
	 * The highest bit set index that was written to.
	 */
	private int max = 0;
	
	/**
	 * Constructs a new bit set with space for at least the given number of bits.
	 * @param size The initial bit set size.
	 */
	public RangeBitSet(int size){
		data = new BitSet(size);
	}
	
	/**
	 * Sets the bit at the given index and updates
	 * the range of used bits.
	 * @param index The index of the bit to set.
	 */
	public void rangeSet(int index){
		data.set(index);
		min = Math.min(min, index);
		max = Math.max(max, index);
	}
	
	/**
	 * Gets the bit at the given index.
	 * @param index The index of the bit to read.
	 * @return True if the bit at the given index was set.
	 */
	public boolean get(int index){
		return data.get(index);
	}
	
	/**
	 * Clears the bits in the given range.
	 * @param from The start of the range (inclusive).
	 * @param to The end of the range (exclusive).
	 */
	public void clear(int from, int to){
		if(to >= from){
			data.clear(from, to + 1);
		}
	}
	
	/**
	 * Clears all bits in the entire bit set.
	 */
	public void clear(){
		data.clear();
		min = Integer.MAX_VALUE;
		max = 0;
	}
	
	/**
	 * Clears all bits that were marked as used since
	 * the last clear by the {@link #rangeSet(int)} operation.
	 * The range of used bits is also reset.
	 */
	public void rangeClear(){
		clear(min, max);
		min = Integer.MAX_VALUE;
		max = 0;
	}
	
	/**
	 * Gets the number of bits that are set in the entire bit set.
	 * @return The number of bits set in the entire bit set.
	 */
	public int count(){
		return data.cardinality();
	}
}
