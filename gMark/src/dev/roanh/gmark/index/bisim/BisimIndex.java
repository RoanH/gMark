package dev.roanh.gmark.index.bisim;

import java.util.List;

import dev.roanh.gmark.type.schema.Predicate;
import dev.roanh.gmark.util.RangeList;

public class BisimIndex{
	/**
	 * The value of k (the CPQ diameter) this index was computed for.
	 */
	private final int k;
	/**
	 * List of predicates (labels) that appear in this index by ID.
	 */
	private final RangeList<Predicate> predicates;
	/**
	 * List of all blocks in the final layer of this index.
	 * This is the layer for k equal to {@link #k}.
	 */
	private final List<BisimBlock> blocks;
	
	protected BisimIndex(int k, List<BisimBlock> blocks, RangeList<Predicate> predicates){
		this.k = k;
		this.blocks = blocks;
		this.predicates = predicates;
	}
	
	/**
	 * Gets the value of k (the CPQ diameter) this index was computed for.
	 * @return The k value for this index.
	 */
	public final int getK(){
		return k;
	}
	
	/**
	 * Gets all the level k blocks in this index.
	 * @return All the blocks in this index.
	 */
	public final List<BisimBlock> getBlocks(){
		return blocks;
	}
}
