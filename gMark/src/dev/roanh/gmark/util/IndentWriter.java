package dev.roanh.gmark.util;

/**
 * Writes that prefixes all lines with
 * a configurable number of spaces and
 * can be converted to a string once all
 * content has been written.
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
	private StringBuffer content;
	/**
	 * The current indent (in spaces).
	 */
	private int indent;
	/**
	 * True if the next write call is the
	 * start of a new line and should thus
	 * be prefixed with spaces.
	 */
	private boolean newLine = true;
	
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
		content = new StringBuffer();
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
	
	@Override
	public String toString(){
		return content.toString();
	}
}
