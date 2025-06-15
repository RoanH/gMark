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
package dev.roanh.gmark.util;

/**
 * Writes that prefixes all lines with a configurable number of spaces
 * and can be converted to a string once all content has been written.
 * @author Roan
 */
public class IndentWriter{
	/**
	 * Buffer of white space characters for copying.
	 */
	private static final char[] BUFFER = new char[]{' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '};
	/**
	 * The context of this writer (all written lines).
	 */
	private final StringBuilder content;
	/**
	 * The current indent (in spaces).
	 */
	private int indent;
	/**
	 * True if the next write call is the start of a
	 * new line and should thus be prefixed with spaces.
	 */
	private boolean newLine = true;
	/**
	 * The position of the current mark in the buffer.
	 */
	private int mark = -1;
	
	/**
	 * Constructs a new indent writer
	 * with an indent of 0.
	 * @see #IndentWriter(int)
	 */
	public IndentWriter(){
		this(0);
	}
	
	/**
	 * Constructs a new indent writer
	 * with the given indent.
	 * @param indent The writer indent.
	 */
	public IndentWriter(int indent){
		content = new StringBuilder();
		this.indent = indent;
	}
	
	/**
	 * Gets the current writer indent.
	 * @return The indent (in spaces).
	 */
	public int getIndent(){
		return indent;
	}
	
	/**
	 * Adds the given string to this writer.
	 * @param str The string to print.
	 */
	public void print(String str){
		if(newLine){
			for(int i = 0; i < indent; i += BUFFER.length){
				content.append(BUFFER, 0, Math.min(BUFFER.length, indent - i));
			}
			newLine = false;
		}
		content.append(str);
	}
	
	/**
	 * Adds the given string to this writer and
	 * adds a line break.
	 * @param str The string to print.
	 */
	public void println(String str){
		print(str);
		println();
	}
	
	/**
	 * Writes a line break.
	 */
	public void println(){
		content.append('\n');
		newLine = true;
	}
	
	/**
	 * Adds the given string to this writer and
	 * adds a line break. After that increases
	 * the current indent by the given amount.
	 * @param str The string to print.
	 * @param indentIncrease The amount to increase
	 *        the indent by.
	 */
	public void println(String str, int indentIncrease){
		println(str);
		indent += indentIncrease;
	}
	
	/**
	 * Decreases the current indent by the given
	 * amount and then adds the given string to
	 * this writer and adds a line break.
	 * @param str The string to print.
	 * @param indentDecrease The amount to decrease
	 *        the indent by.
	 */
	public void println(int indentDecrease, String str){
		indent -= indentDecrease;
		println(str);
	}
	
	/**
	 * Increases the current indent by the given
	 * number of spaces.
	 * @param n The number of spaces to
	 *        increase the indent by.
	 */
	public void increaseIndent(int n){
		indent += n;
	}
	
	/**
	 * Decreases the current indent by the given
	 * number of spaces.
	 * @param n The number of spaces to
	 *        decrease the indent by.
	 */
	public void decreaseIndent(int n){
		indent -= n;
	}
	
	/**
	 * Sets the indent to the given number of spaces.
	 * @param n The new indent.
	 */
	public void setIndent(int n){
		indent = n;
	}
	
	/**
	 * Adds the given integer to this writer.
	 * @param i The integer to print.
	 */
	public void print(int i){
		print(String.valueOf(i));
	}
	
	/**
	 * Adds the given boolean to this writer.
	 * @param value The boolean to print.
	 */
	public void print(boolean value){
		print(String.valueOf(value));
	}
	
	/**
	 * Marks the current end of the buffer for later use.
	 * @see #deleteFromMark(int)
	 * @see #deleteAllFromMark()
	 */
	public void mark(){
		mark = content.length();
	}

	/**
	 * Deletes the give number of characters starting from the marked position.
	 * @param n The number of characters to delete.
	 * @see #mark()
	 */
	public void deleteFromMark(int n){
		content.delete(mark, mark + n);
	}
	
	/**
	 * Deletes all content written after the mark was set.
	 * @see #mark()
	 */
	public void deleteAllFromMark(){
		content.delete(mark, content.length());
	}
	
	@Override
	public String toString(){
		return content.toString();
	}
}
