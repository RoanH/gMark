package dev.roanh.gmark.exception;

/**
 * Exception throw when there are issues
 * parsing a configuration file.
 * @author Roan
 */
public class ConfigException extends Exception{
	/**
	 * Serial ID.
	 */
	private static final long serialVersionUID = -2229786583456473669L;

	/**
	 * Constructs a new configuration exception
	 * with the given root cause exception.
	 * @param cause The original exception.
	 */
	public ConfigException(Exception cause){
		super(cause.getMessage(), cause);
	}
	
	/**
	 * Construts a new configuration exception
	 * with the given exception message.
	 * @param msg The exception message.
	 */
	public ConfigException(String msg){
		super(msg);
	}
}
