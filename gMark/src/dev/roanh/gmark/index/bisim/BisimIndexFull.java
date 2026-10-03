package dev.roanh.gmark.index.bisim;

import java.util.List;

import dev.roanh.gmark.type.schema.Predicate;
import dev.roanh.gmark.util.RangeList;

public class BisimIndexFull extends BisimIndex{
	private final RangeList<List<BisimBlock>> layers;

	protected BisimIndexFull(int k, RangeList<List<BisimBlock>> layers, RangeList<Predicate> predicates){
		super(k, layers.get(k - 1), predicates);
		this.layers = layers;
	}
	
	/**
	 * Gets all the blocks in this index at the requested layer.
	 * @param k The layer to get computed blocks for.
	 * @return All the blocks in this index at layer k.
	 */
	public final List<BisimBlock> getBlocks(int k){
		return layers.get(k - 1);
	}
}
