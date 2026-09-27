package dev.roanh.gmark.index.bisim;

/**
 * Progress listener for k-path-bisimulation indices.
 * @author Roan
 */
public abstract interface BisimProgressListener{
	/**
	 * Default listener that ignores all events.
	 */
	public static final BisimProgressListener NONE = new BisimProgressListener(){
		
		@Override
		public void partitionStart(int k){
		}
		
		@Override
		public void partitionEnd(int k){
		}
		
		@Override
		public void partitionCombinationStart(int k1, int k2){
		}
		
		@Override
		public void partitionCombinationEnd(int k1, int k2){
		}
		
		@Override
		public void computeBlocksStart(int k){
		}
		
		@Override
		public void computeBlocksEnd(int k){
		}
	};
	
	/**
	 * Called when graph partitioning for a new layer starts.
	 * @param k The diameter for the layer being partitioned.
	 */
	public abstract void partitionStart(int k);
	
	/**
	 * Called when partitions are constructed from two previous
	 * blocks from a different layer (start).
	 * @param k1 The diameter of the first block.
	 * @param k2 The diameter of the second block.
	 */
	public abstract void partitionCombinationStart(int k1, int k2);
	
	/**
	 * Called when partitions are constructed from two previous
	 * blocks from a different layer (end).
	 * @param k1 The diameter of the first block.
	 * @param k2 The diameter of the second block.
	 */
	public abstract void partitionCombinationEnd(int k1, int k2);
	
	/**
	 * Called when graph partitioning for a new layer ends.
	 * @param k The diameter for the layer that was partitioned.
	 */
	public abstract void partitionEnd(int k);
	
	/**
	 * Called when blocks are being computed for a new index layer.
	 * @param k The diameter for the layer that blocks are being computed for.
	 */
	public abstract void computeBlocksStart(int k);
	
	/**
	 * Called when blocks are done being computed for a new index layer.
	 * @param k The diameter for the layer that blocks were computed for.
	 */
	public abstract void computeBlocksEnd(int k);
}
