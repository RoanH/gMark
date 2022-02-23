package dev.roanh.gmark.exception;

public class ConfigException extends Exception{
	/**
	 * Serial ID.
	 */
	private static final long serialVersionUID = -2229786583456473669L;

	public ConfigException(Exception cause){
		super(cause.getMessage(), cause);
	}
	
	public ConfigException(String msg){
		super(msg);
	}
}
