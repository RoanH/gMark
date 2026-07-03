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

import dev.roanh.cpqindex.Index.Block;
import dev.roanh.gmark.util.RangeList;

/**
 * Simple record representing a pair of blocks.
 * @author Roan
 * @param first The first block.
 * @param second The second block.
 */
public final record BlockPair(Block first, Block second){
	
	/**
	 * Constructs a new block pair from the given path pair.
	 * @param pair The path pair to get the blocks from.
	 */
	public BlockPair(PathPair pair){
		this(pair.getFirst().getBlock(), pair.getSecond().getBlock());
	}
	
	/**
	 * Reads a previously saved block pair from the given stream.
	 * @param in The stream to read from.
	 * @param blockMap A map of blocks read so far by ID.
	 * @throws IOException When an IOException occurs.
	 */
	public BlockPair(DataInputStream in, RangeList<Block> blockMap) throws IOException{
		this(blockMap.get(in.readInt()), blockMap.get(in.readInt()));
	}
	
	/**
	 * Writes this block pair to the given stream.
	 * @param out The stream to write to.
	 * @throws IOException When an IOException occurs.
	 */
	public void write(DataOutputStream out) throws IOException{
		out.writeInt(first.getId());
		out.writeInt(second.getId());
	}
}