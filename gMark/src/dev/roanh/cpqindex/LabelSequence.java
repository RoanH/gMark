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
import java.util.Arrays;

import dev.roanh.gmark.type.schema.Predicate;
import dev.roanh.gmark.util.RangeList;

/**
 * Class representing a sequence of edge labels.
 * @author Roan
 */
public final class LabelSequence implements Comparable<LabelSequence>{
	/**
	 * An ordered array of the labels that make up the label sequence.
	 */
	private Predicate[] data;
	
	/**
	 * Constructs a new label sequence by joining the given sequences.
	 * @param first The start of the new label sequence.
	 * @param last The end of the new label sequence.
	 */
	public LabelSequence(LabelSequence first, LabelSequence last){
		data = new Predicate[first.data.length + last.data.length];
		System.arraycopy(first.data, 0, data, 0, first.data.length);
		System.arraycopy(last.data, 0, data, first.data.length, last.data.length);
	}
	
	/**
	 * Constructs a new label sequence with just a single label.
	 * @param label The single label of the sequence.
	 */
	public LabelSequence(Predicate label){
		data = new Predicate[]{label};
	}
	
	/**
	 * Reads a label sequence from the given input stream and
	 * resolves the corresponding predicates from the given list.
	 * @param in The stream to read from.
	 * @param labels A list with each predicate at its ID position.
	 * @throws IOException When an IO exception occurs.
	 */
	public LabelSequence(DataInputStream in, RangeList<Predicate> labels) throws IOException{
		data = new Predicate[in.readInt()];
		for(int i = 0; i < data.length; i++){
			int id = in.readInt();
			if(id < 0){
				data[i] = labels.get(-id - 1).getInverse();
			}else{
				data[i] = labels.get(id);
			}
		}
	}

	/**
	 * Writes this label sequence to the given stream.
	 * @param out The stream to write to.
	 * @throws IOException When an IOException occurs.
	 */
	public void write(DataOutputStream out) throws IOException{
		out.writeInt(data.length);
		for(Predicate p : data){
			out.writeInt(p.isInverse() ? (-p.getID() - 1) : p.getID());
		}
	}
	
	/**
	 * Gets the label in this sequence as an array of predicates.
	 * @return The labels for this sequence.
	 */
	public Predicate[] getLabels(){
		return data;
	}
	
	@Override
	public String toString(){
		StringBuilder builder = new StringBuilder();
		for(Predicate p : data){
			builder.append(p.getAlias());
		}
		return builder.toString();
	}

	@Override
	public int compareTo(LabelSequence o){
		int cmp = Integer.compare(data.length, o.data.length);
		if(cmp == 0){
			for(int i = 0; i < data.length; i++){
				cmp = data[i].compareTo(o.data[i]);
				if(cmp != 0){
					return cmp;
				}
			}
			
			return 0;
		}else{
			return cmp;
		}
	}
	
	@Override
	public int hashCode(){
		return Arrays.hashCode(data);
	}
	
	@Override
	public boolean equals(Object obj){
		return Arrays.equals(data, ((LabelSequence)obj).data);
	}
}