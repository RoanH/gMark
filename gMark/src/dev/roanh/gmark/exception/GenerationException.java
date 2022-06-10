/*
 * gMark: A domain- and query language-independent graph instance and query workload generator.
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
	
	/**
	 * Executes the given runnable and rethrows any exception
	 * it throws as a new {@link #GenerationException}.
	 * @param runnable The runnable to execute.
	 * @throws GenerationException The rethrown exception if
	 *         executing the runnable threw an exception.
	 */
	public static final void rethrow(GenerationRunnable runnable) throws GenerationException{
		try{
			runnable.run();
		}catch(Exception e){
			throw new GenerationException(e.getMessage(), e);
		}
	}
	
	/**
	 * Runnable interface that can throw exceptions.
	 * @author Roan
	 */
	public static abstract interface GenerationRunnable{
		
		/**
		 * Executes this runnable.
		 * @throws Exception When some exception occurs.
		 */
		public abstract void run() throws Exception;
	}
}
