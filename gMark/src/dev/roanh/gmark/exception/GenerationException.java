package dev.roanh.gmark.exception;

/**
 * Exception thrown when something goes wrong when
 * randomly generating something.
 * @author Roan
 */
public class GenerationException extends Exception{
	/**
	 * Serial ID.
	 */
	private static final long serialVersionUID = 4496821367499497928L;

	/**
	 * Constructs a new generation exception with
	 * the given reason.
	 * @param reason The exception reason.
	 */
	public GenerationException(String reason){
		super(reason);
	}
	
	/**
	 * Constructs a new generation exception with
	 * the given reason and cause.
	 * @param reason The exception reason.
	 * @param cause The original exception.
	 */
	public GenerationException(String reason, Throwable cause){
		super(reason, cause);
	}
	
	public static final void rethrow(GenerationRunnable runnable) throws GenerationException{
		try{
			runnable.run();
		}catch(Exception e){
			throw new GenerationException(e.getMessage(), e);
		}
	}
	
	public static abstract interface GenerationRunnable{
		
		public abstract void run() throws Exception;
	}
}
