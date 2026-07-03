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

import java.util.StringJoiner;

/**
 * Utility class for writing a sequence of integers to an array
 * while using a variable number of bits for each integer.
 * @author Roan
 */
public class BitWriter{
	/**
	 * The data array being written to.
	 */
	private byte[] data;
	/**
	 * The current byte in the data array being written.
	 */
	private int pos = 0;
	/**
	 * The next bit in the data array to write to.
	 */
	private int sub = 8;
	
	/**
	 * Constructs a new BitWriter with enough space for
	 * at least the requested number of bits.
	 * @param bits The minimum number of bits to allocate
	 *        space for in the data array.
	 */
	public BitWriter(int bits){
		int floor = Math.floorDiv(bits, 8);
		if(floor * 8 != bits){
			floor++;
		}
		
		data = new byte[floor];
	}
	
	/**
	 * Writes the bits of the given integer using
	 * exactly the given number of bits.
	 * @param i The integer to write.
	 * @param bits The number of bit to use to
	 *        write the given integer.
	 */
	public void writeInt(int i, int bits){
		while(bits > 0){
			if(sub >= bits){
				//fully fits
				sub -= bits;
				data[pos] |= (i & ((1 << bits) - 1)) << sub;
				if(sub == 0){
					pos++;
					sub = 8;
				}
				
				//bits always 0
				return;
			}else{
				//need to split
				bits -= sub;
				data[pos] |= (i >>> bits) & ((1 << sub) - 1);
				pos++;
				sub = 8;
			}
		}
	}
	
	/**
	 * Gets the array containing the written bits.
	 * @return The data array being written to.
	 */
	public byte[] getData(){
		return data;
	}
	
	/**
	 * Constructs a string version of the bits written.
	 * @return A string with the written bits.
	 */
	public String toBinaryString(){
		StringJoiner buf = new StringJoiner(" ");
		for(byte b : data){
			buf.add(String.format("%1$8s", Integer.toBinaryString(Byte.toUnsignedInt(b))).replace(' ', '0'));
		}
		return buf.toString();
	}
}
