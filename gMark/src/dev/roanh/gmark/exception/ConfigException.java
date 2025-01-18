/*
 * gMark: A domain- and query language-independent query workload generator and query language utility library.
 * Copyright (C) 2021  Roan Hofland (roan@roanh.dev).  All rights reserved.
 * GitHub Repository: https://github.com/RoanH/gMark
 *
 * gMark is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * gMark is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
	public ConfigException(Throwable cause){
		super(cause.getMessage(), cause);
	}
	
	/**
	 * Constructs a new configuration exception
	 * with the given exception message.
	 * @param msg The exception message.
	 */
	public ConfigException(String msg){
		super(msg);
	}
}
