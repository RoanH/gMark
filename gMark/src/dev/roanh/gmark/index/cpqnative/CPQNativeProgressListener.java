package dev.roanh.gmark.index.cpqnative;

import dev.roanh.gmark.index.bisim.BisimProgressListener;

public abstract interface CPQNativeProgressListener extends BisimProgressListener{
	/**
	 * Default listener that ignores all events.
	 */
	public static final CPQNativeProgressListener NONE = new CPQNativeProgressListener(){
		
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

		@Override
		public void coresBlocksDone(int done, int total){
		}

		@Override
		public void coresStart(int k){
		}

		@Override
		public void coresEnd(int k){
		}

		@Override
		public void mapStart(){
		}

		@Override
		public void mapEnd(){
		}

		@Override
		public void intermediateProgress(long cores, int blockDone, int totalBlocks){
		}
	};
	
	/**
	 * Called when cores for a new layer start being computed.
	 * @param k The diameter for the layer cores are computed for.
	 */
	public abstract void coresStart(int k);
	
	/**
	 * Intermediate core computation progress update.
	 * @param done Total number of computed blocks.
	 * @param total The number of blocks to compute in total.
	 */
	public abstract void coresBlocksDone(int done, int total);

	/**
	 * Called when cores for a new layer are done being computed.
	 * @param k The diameter for the layer cores were computed for.
	 */
	public abstract void coresEnd(int k);
	
	/**
	 * Called when mapping cores to blocks starts.
	 */
	public abstract void mapStart();
	
	/**
	 * Called when mapping cores to blocks is done.
	 */
	public abstract void mapEnd();
	
	/**
	 * Logs an intermediate progress update.
	 * @param cores The total number of cores computed so far.
	 * @param blockDone The total number of blocks done.
	 * @param totalBlocks The total number of blocks.
	 */
	public abstract void intermediateProgress(long cores, int blockDone, int totalBlocks);
}
