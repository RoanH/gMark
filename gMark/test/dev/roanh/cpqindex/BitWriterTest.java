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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class BitWriterTest{

	@Test
	public void longTest(){
		BitWriter w = new BitWriter(64);

		assertEquals("00000000 00000000 00000000 00000000 00000000 00000000 00000000 00000000", w.toBinaryString());
		assertArrayEquals(new byte[]{0, 0, 0, 0, 0, 0, 0, 0}, w.getData());

		w.writeInt(0b110, 3);
		assertEquals("11000000 00000000 00000000 00000000 00000000 00000000 00000000 00000000", w.toBinaryString());
		assertArrayEquals(new byte[]{-64, 0, 0, 0, 0, 0, 0, 0}, w.getData());

		w.writeInt(0b1110, 3);
		assertEquals("11011000 00000000 00000000 00000000 00000000 00000000 00000000 00000000", w.toBinaryString());
		assertArrayEquals(new byte[]{-40, 0, 0, 0, 0, 0, 0, 0}, w.getData());
		
		w.writeInt(0b1111, 4);
		assertEquals("11011011 11000000 00000000 00000000 00000000 00000000 00000000 00000000", w.toBinaryString());
		assertArrayEquals(new byte[]{-37, -64, 0, 0, 0, 0, 0, 0}, w.getData());
		
		w.writeInt(0b01111111, 8);
		assertEquals("11011011 11011111 11000000 00000000 00000000 00000000 00000000 00000000", w.toBinaryString());
		assertArrayEquals(new byte[]{-37, -33, -64, 0, 0, 0, 0, 0}, w.getData());
		
		w.writeInt(0b10111111_11111111_11111111_11111101, 32);
		assertEquals("11011011 11011111 11101111 11111111 11111111 11111111 01000000 00000000", w.toBinaryString());
		assertArrayEquals(new byte[]{-37, -33, -17, -1, -1, -1, 64, 0}, w.getData());
		
		w.writeInt(0, 4);
		assertEquals("11011011 11011111 11101111 11111111 11111111 11111111 01000000 00000000", w.toBinaryString());
		assertArrayEquals(new byte[]{-37, -33, -17, -1, -1, -1, 64, 0}, w.getData());
		
		w.writeInt(1, 1);
		assertEquals("11011011 11011111 11101111 11111111 11111111 11111111 01000010 00000000", w.toBinaryString());
		assertArrayEquals(new byte[]{-37, -33, -17, -1, -1, -1, 66, 0}, w.getData());
	}
}
