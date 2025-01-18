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
package dev.roanh.gmark.data;

/**
 * Combination of a source and target vertex implicitly representing
 * the existence of some path between these two vertices.
 * @author Roan
 * @param source The source vertex of the path.
 * @param target The target vertex of the path.
 */
public record SourceTargetPair(int source, int target) implements Comparable<SourceTargetPair>{

	@Override
	public String toString(){
		return "(" + source + ", " + target + ")";
	}
	
	@Override
	public int compareTo(SourceTargetPair other){
		int cmp = Integer.compare(source, other.source());
		return cmp == 0 ? Integer.compare(target, other.target()) : cmp;
	}
}
