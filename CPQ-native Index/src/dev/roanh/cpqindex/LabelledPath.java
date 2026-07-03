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

import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import dev.roanh.cpqindex.Index.Block;
import dev.roanh.gmark.type.schema.Predicate;

/**
 * Represents a path through the graph identified by
 * a source target node pair and a number of label
 * sequences that exist between that node pair. This
 * object is also known as a segment for layers of the
 * index with k > 1.
 * @author Roan
 */
public final class LabelledPath{
	/**
	 * The node pair for this labelled path. All stored label
	 * sequences are between the vertices of this pair.
	 */
	private final Pair pair;
	/**
	 * The label sequences that were found that exist between
	 * the vertices of the node pair for this path.
	 */
	private final SortedSet<LabelSequence> labels = new TreeSet<LabelSequence>();
	/**
	 * For k > 1 segments are constructed by combining segments from previous layers.
	 * The combinations of segments this segment was constructed from are in this set.
	 */
	private final SortedSet<PathPair> segs = new TreeSet<PathPair>();
	/**
	 * The ancestor of this segment if any. The ancestor is the segment in the previous index
	 * layer that had the same path pair as this segment.
	 */
	private final LabelledPath ancestor;
	/**
	 * After blocks have been computed this is the ID of the block this segment belong to.
	 * The value will be -1 before that. Segments with equal IDs are in the same block.
	 */
	private int segId = -1;
	/**
	 * After block are computed this is the block this segment is contained in.
	 */
	private Block block;
	/**
	 * Precomputed hash code value for this segment.
	 */
	private int segHash;
	
	/**
	 * Constructs a new labelled path with the given pair and ancestor.
	 * @param pair The path pair for this segment.
	 * @param ancestor The segment in the previous index layer with the same path pair or null.
	 */
	public LabelledPath(Pair pair, LabelledPath ancestor){
		this.pair = pair;
		this.ancestor = ancestor;
	}
	
	/**
	 * Compares this labelled path against the given other labelled path based on the set of labels.
	 * @param other The other labelled path to compare with.
	 * @return A value of 0 if both paths are equal, a value less
	 *         than 0 if this path is less than the other segment
	 *         and a value greater than 0 if this path is greater.
	 * @see #labels
	 */
	public int compareLabelsTo(LabelledPath other){
		return compare(labels, other.labels);
	}
	
	/**
	 * Compares this segment against the given other segment based on the path pair.
	 * @param other The other labelled path to compare with.
	 * @return A value of 0 if both paths are equal, a value less
	 *         than 0 if this path is less than the other segment
	 *         and a value greater than 0 if this path is greater.
	 * @see #pair
	 */
	public int comparePathTo(LabelledPath other){
		return pair.compareTo(other.pair);
	}
	
	/**
	 * Gets all the label sequences for this labelled path.
	 * @return All label sequence for this labelled path.
	 */
	public Set<LabelSequence> getLabels(){
		return labels;
	}
	
	/**
	 * Gets the pair of path for this segment.
	 * @return The path for this segment.
	 */
	public Pair getPair(){
		return pair;
	}
	
	/**
	 * Gets all segment combinations that create this segment.
	 * @return All segments for this segment.
	 */
	public Set<PathPair> getSegments(){
		return segs;
	}
	
	/**
	 * Gets the ancestor segment for this segment. The ancestor segment is
	 * a segment from the previous index layer with the same path.
	 * @return The ancestor segment if any.
	 */
	public LabelledPath getAncestor(){
		return ancestor;
	}
	
	/**
	 * Checks if this segment has an ancestor segment in the
	 * previous index layer. The ancestor segment has an identical path.
	 * @return True if this segment has an ancestor.
	 */
	public boolean hasAncestor(){
		return ancestor != null;
	}
	
	/**
	 * Sets the block for this segment.
	 * @param block The block this segment is in.
	 */
	public void setBlock(Block block){
		this.block = block;
	}
	
	/**
	 * Sets the segment ID for this for segment. This ID will
	 * later determine the ID of the block for this segment.
	 * @param id The ID for thsi segment.
	 */
	public void setSegmentId(int id){
		segId = id;
	}
	
	/**
	 * Gets the source vertex of the path for this segment.
	 * @return The source vertex.
	 */
	public int getSource(){
		return pair.getSource();
	}
	
	/**
	 * Gets the target vertex of the path for this segment.
	 * @return The target vertex.
	 */
	public int getTarget(){
		return pair.getTarget();
	}
	
	/**
	 * Tests if the label sequences for this path are
	 * equal to the labels for the given other path.
	 * @param other The other path to compare with.
	 * @return True if both paths have identical label sequences.
	 */
	public boolean equalLabels(LabelledPath other){
		return labels.equals(other.labels);
	}
	
	/**
	 * Gets the ID of the block this segment is in.
	 * @return The ID of the block for this segment.
	 */
	public int getSegmentId(){
		return segId;
	}
	
	/**
	 * Gets the block this segment is in, if set.
	 * @return The block this segment is in.
	 */
	public Block getBlock(){
		return block;
	}
	
	/**
	 * Computes and caches the hashcode of this segment
	 * to improve sorting performance.
	 */
	public void cacheHashCode(){
		segHash = segs.hashCode();
	}

	/**
	 * Compares this segment against the given other segment based
	 * on the set of segment combinations they were constructed from.
	 * Note that {@link #cacheHashCode()} should either have been
	 * called on both segments first or on neither.
	 * @param other The other segment to compare with.
	 * @return A value of 0 if both segments are equal, a value less
	 *         than 0 if this segment is less than the other segment
	 *         and a value greater than 0 if this segment is greater.
	 * @see #segs
	 */
	public int compareSegmentsTo(LabelledPath other){
		int cmp = Integer.compare(segHash, other.segHash);
		if(cmp != 0){
			return cmp;
		}
		
		cmp = Boolean.compare(ancestor == null, other.ancestor == null);
		if(cmp != 0){
			return cmp;
		}
		
		if(ancestor != null){
			cmp = Integer.compare(ancestor.segId, other.ancestor.segId);
			if(cmp != 0){
				return cmp;
			}
		}
		
		return compare(segs, other.segs);
	}
	
	/**
	 * Compares two sorted sets of elements.
	 * @param <T> The set data type.
	 * @param a The first set
	 * @param b The second set.
	 * @return A value of 0 if both sets are equal, a value less
	 *         than 0 if the first set is less than the other set
	 *         and a value greater than 0 if the first set is greater.
	 */
	private <T extends Comparable<T>> int compare(SortedSet<T> a, SortedSet<T> b){
		int cmp = Integer.compare(a.size(), b.size());
		if(cmp == 0){
			Iterator<T> iter = b.iterator();
			for(T seg : a){
				cmp = seg.compareTo(iter.next());
				if(cmp != 0){
					return cmp;
				}
			}
			
			return 0;
		}else{
			return cmp;
		}
	}
	
	/**
	 * Adds a new construction segment combination to this segment.
	 * Note that order matters as the paths are joined.
	 * @param first The first segment of the combination.
	 * @param last The second segment of the combination.
	 * @see #segs
	 */
	public void addSegment(LabelledPath first, LabelledPath last){
		segs.add(new PathPair(first, last));
	}
	
	/**
	 * Adds a single label to this labelled path.
	 * @param label The label to add.
	 * @see #labels
	 */
	public void addLabel(Predicate label){
		labels.add(new LabelSequence(label));
	}
	
	/**
	 * Adds a new label sequence to this path by concatenating
	 * the two given sequences.
	 * @param first The first label sequence.
	 * @param last The second label sequence to concatenate after the first.
	 * @see #labels
	 */
	public void addLabel(LabelSequence first, LabelSequence last){
		labels.add(new LabelSequence(first, last));
	}
	
	/**
	 * Tests if this segment is a loop, which means its pair is a loop.
	 * @return True if this segment is a loop.
	 * @see Pair#isLoop()
	 */
	public boolean isLoop(){
		return pair.isLoop();
	}
	
	@Override
	public String toString(){
		StringBuilder builder = new StringBuilder();
		builder.append("LabelledPath[id=");
		builder.append(segId);
		builder.append(",path=");
		builder.append(pair);
		builder.append(",labels={");
		for(LabelSequence seq : labels){
			builder.append(seq.toString());
			builder.append(",");
		}
		builder.delete(builder.length() - 1, builder.length());
		builder.append("},segs={");
		for(PathPair seq : segs){
			builder.append(seq.getFirst().segId);
			builder.append(seq.getSecond().segId);
			builder.append(",");
		}
		builder.delete(builder.length() - 1, builder.length());
		builder.append("}]");
		builder.append("}]");
		return builder.toString();
	}
	
	@Override
	public int hashCode(){
		return segId;
	}
	
	@Override
	public boolean equals(Object obj){
		return segId == ((LabelledPath)obj).segId;
	}
}