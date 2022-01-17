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

	public GenerationException(String reason){
		super(reason);
	}
	
	public GenerationException(String reason, Throwable cause){
		super(reason, cause);
	}
}
