package dev.roanh.gmark.index.cpqnative;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ListIterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import dev.roanh.cpqindex.Index.Block;
import dev.roanh.cpqindex.ProgressListener;
import dev.roanh.gmark.index.bisim.BisimIndex;
import dev.roanh.gmark.index.bisim.BisimIndexCalculator;
import dev.roanh.gmark.type.schema.Predicate;
import dev.roanh.gmark.util.graph.generic.UniqueGraph;
import dev.roanh.nauty.api.NautyApi;

public class CPQNativeIndexCalculator{
	private final BisimIndexCalculator bisimSettings = new BisimIndexCalculator();

	private CPQNativeProgressListener progress = CPQNativeProgressListener.NONE;
	private int k;
	private int threads;

	public void setComputeLabels(boolean computeLabels){
		bisimSettings.setComputeLabels(computeLabels);
	}
	
	public void setK(int k){
		bisimSettings.setK(k);
		this.k = k;
	}
	
	public void setThreads(int threads){
		this.threads = threads;
	}
	
	public void setSorted(boolean sorted){
		bisimSettings.setSorted(sorted);
	}
	
	public void setProgressListener(CPQNativeProgressListener listener){
		bisimSettings.setProgressListener(listener);
		progress = listener;
	}
	
	
	
	
	
	
	
	public CPQNativeIndex compute(UniqueGraph<Integer, Predicate> g) throws IllegalArgumentException{
		return compute(bisimSettings.compute(g));
	}
	
	public CPQNativeIndex compute(BisimIndex base) throws IllegalArgumentException{
		if(k != base.getK()){
			throw new IllegalArgumentException("Base index k differs from the requested diameter.");
		}
		
		
		//TODO
		return null;
		
	}
	
	/**
	 * Computes CPQ cores for each block in this index. Note that if this index
	 * was saved and read back that it is only possible to compute cores if the
	 * index was fully saved with extra state information.
	 * @param threads The number of CPU threads to use to compute cores.
	 * @throws InterruptedException When the current thread is interrupted.
	 * @throws IllegalStateException When cores have already been computed for
	 *         this index of when this index is read back and was not fully saved.
	 * @see #setIntersections(int)
	 * @see #setProgressListener(ProgressListener)
	 * @see #write(OutputStream, boolean)
	 * @see #Index(InputStream)
	 */
	private final void computeCores() throws InterruptedException, IllegalStateException{
		if(computeCores){
			throw new IllegalStateException("Cores have already been computed.");
		}else if(predicates == null){
			throw new IllegalStateException("Cannot compute cores on an index that wasn't fully saved.");
		}
		
		ExecutorService executor = Executors.newFixedThreadPool(threads);
		ThreadLocal<NautyApi> nauty = ThreadLocal.withInitial(NautyApi::new);

		//process cores layer by layer
		for(int i = 0; i < k; i++){
			progress.coresStart(i + 1);
			
			final int total = layers.get(i).size();
			Lock lock = new ReentrantLock();
			Condition cond = lock.newCondition();
			AtomicInteger done = new AtomicInteger(0);
			ListIterator<Block> iter = layers.get(i).listIterator(total);
			while(iter.hasPrevious()){
				Block block = iter.previous();
				executor.execute(()->{
					try{
						block.computeCores(nauty.get());

						if(done.incrementAndGet() == total){
							lock.lock();
						}else if(!lock.tryLock()){
							return;
						}

						try{
							cond.signal();
						}finally{
							lock.unlock();
						}
					}catch(Throwable t){
						System.err.println("FATAL");
						t.printStackTrace();
						progress.intermediateProgress(-1, -1, -1);
					}
				});
			}
			
			long lastUpdate = 0;
			while(true){
				try{
					lock.lock();
					if(cond.await(10, TimeUnit.MINUTES)){
						int val = done.get();
						progress.coresBlocksDone(val, total);
						if(val == total){
							break;
						}
					}

					if(lastUpdate < System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(10)){
						progress.intermediateProgress(blocks.stream().mapToInt(b->b.canonCores.size()).summaryStatistics().getSum(), done.get(), total);
						lastUpdate = System.currentTimeMillis();
					}
				}finally{
					lock.unlock();
				}
			}
			
			progress.coresEnd(i + 1);
		}
		
		executor.shutdown();
		computeCores = true;
		mapCoresToBlocks();
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
