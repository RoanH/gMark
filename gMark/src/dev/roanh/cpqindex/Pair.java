/*
 * CPQ-native Index: A graph database index with native support for CPQs.
 * Copyright (C) 2023  Roan Hofland (roan@roanh.dev).  All rights reserved.
 * GitHub Repository: https://github.com/RoanH/CPQ-native-index
 *
 * CPQ-native Index is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CPQ-native Index is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package dev.roanh.cpqindex;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Represents a pair of two vertices, also referred to as a
 * path or an st-pair.
 * @author Roan
 */
public final class Pair implements Comparable<Pair>{
	/**
	 * The source vertex.
	 */
	private final int src;
	/**
	 * The target vertex.
	 */
	private final int trg;
	
	/**
	 * Constructs a new pair with the given source and target vertex.
	 * @param src The source vertex.
	 * @param trg The target vertex.
	 */
	public Pair(int src, int trg){
		this.src = src;
		this.trg = trg;
	}
	
	/**
	 * Reads a pair from the given input stream.
	 * @param in The stream to read from.
	 * @throws IOException When an IOException occurs.
	 */
	public Pair(DataInputStream in) throws IOException{
		src = in.readInt();
		trg = in.readInt();
	}
	
	/**
	 * Write a pair to the given input stream.
	 * @param out The stream to write from.
	 * @throws IOException When an IOException occurs.
	 */
	public void write(DataOutputStream out) throws IOException{
		out.writeInt(src);
		out.writeInt(trg);
	}

	/**
	 * Get the source vertex of this pair.
	 * @return The source vertex of this pair.
	 */
	public int getSource(){
		return src;
	}
	
	/**
	 * Gets the target vertex of this pair.
	 * @return The target vertex of thsi pair.
	 */
	public int getTarget(){
		return trg;
	}
	
	/**
	 * Checks if this pair represents a loop, that is,
	 * if the source and target vertex are equivalent.
	 * @return True if this pair represents a loop.
	 */
	public boolean isLoop(){
		return src == trg;
	}
	
	@Override
	public boolean equals(Object obj){
		Pair other = (Pair)obj;
		return src == other.src && trg == other.trg;
	}

	@Override
	public int hashCode(){
		return 31 * src + trg;
	}
	
	@Override
	public String toString(){
		return "(" + src + "," + trg + ")";
	}

	@Override
	public int compareTo(Pair o){
		int cmp = Integer.compare(src, o.src);
		return cmp == 0 ? Integer.compare(trg, o.trg) : cmp;
	}
}