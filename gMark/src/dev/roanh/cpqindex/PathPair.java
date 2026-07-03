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

/**
 * Represents a pair of two labelled paths
 * that were joined to form a new path.
 * @author Roan
 */
public final class PathPair implements Comparable<PathPair>{
	/**
	 * The first path of this pair, also the start of the joined path.
	 */
	private final LabelledPath first;
	/**
	 * The second path of this pair, also the end of the joined path.
	 */
	private final LabelledPath second;
	
	/**
	 * Constructs a new path pair with the given paths.
	 * @param first The first and start path.
	 * @param second The second and end path.
	 */
	public PathPair(LabelledPath first, LabelledPath second){
		this.first = first;
		this.second = second;
	}
	
	/**
	 * Gets the first path in this pair.
	 * @return The first path in this pair.
	 * @see #first
	 */
	public LabelledPath getFirst(){
		return first;
	}
	
	/**
	 * Gets the second path in this pair.
	 * @return The second path in this pair.
	 * @see #second
	 */
	public LabelledPath getSecond(){
		return second;
	}
	
	@Override
	public boolean equals(Object obj){
		PathPair other = (PathPair)obj;
		return first.equals(other.first) && second.equals(other.second);
	}
	
	@Override
	public int hashCode(){
		return 31 * first.hashCode() + second.hashCode();
	}

	@Override
	public int compareTo(PathPair o){
		int cmp = Integer.compare(first.getSegmentId(), o.first.getSegmentId());
		return cmp == 0 ? Integer.compare(second.getSegmentId(), o.second.getSegmentId()) : cmp;
	}
}