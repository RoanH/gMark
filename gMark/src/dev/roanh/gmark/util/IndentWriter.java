package dev.roanh.gmark.util;

public class IndentWriter{
	private static final char[] BUFFER = new char[]{' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '};
	private StringBuffer content;
	private int indent;
	private boolean newLine = true;
	
	public IndentWriter(){
		this(0);
	}
	
	public IndentWriter(int indent){
		content = new StringBuffer();
		this.indent = indent;
	}
	
	public void append(String str){
		print(str);
	}

	public void print(String str){
		if(newLine){
			for(int i = 0; i < indent; i += BUFFER.length){
				content.append(BUFFER, 0, Math.min(BUFFER.length, indent - i));
			}
			newLine = true;
		}
		content.append(str);
	}
	
	public void println(String str){
		print(str);
		content.append('\n');
		newLine = true;
	}
	
	public void increaseIndent(int n){
		indent += n;
	}
	
	public void decreaseIndent(int n){
		indent -= n;
	}
	
	public void setIndent(int n){
		indent = n;
	}
	
	@Override
	public String toString(){
		return content.toString();
	}
}
