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
package dev.roanh.gmark.util.graph.generic;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Implementation of a tree data structure where data can
 * be stored at each node in the tree and each tree node
 * can have any number of child nodes.
 * @author Roan
 * @param <T> The store data type.
 */
public class Tree<T> implements Iterable<Tree<T>>{
	/**
	 * A list of child nodes for this tree node.
	 */
	private List<Tree<T>> children = new ArrayList<Tree<T>>();
	/**
	 * The parent node for this tree node.
	 */
	private Tree<T> parent = null;
	/**
	 * The data stored at this tree node.
	 */
	private T data;
	
	/**
	 * Constructs a new tree node with the given data to store.
	 * @param data The data to store at this node.
	 */
	public Tree(T data){
		this.data = data;
	}
	
	/**
	 * Invokes the given consumer for all nodes in this tree
	 * that are in the sub tree rooted at this tree node.
	 * @param nodeVisitor The consumer to pass nodes to.
	 * @return True if the search was interrupted early.
	 */
	public boolean forEach(TreeVisitor<T> nodeVisitor){
		if(nodeVisitor.visitNode(this)){
			return true;
		}
		
		for(Tree<T> child : children){
			if(child.forEach(nodeVisitor)){
				return true;
			}
		}
		
		return false;
	}

	/**
	 * Invokes the given consumer for all nodes in this tree
	 * that are in the sub tree rooted at this tree node. It
	 * will be guaranteed that all the child nodes will be
	 * visited before their parent.
	 * @param nodeVisitor The consumer to pass nodes to.
	 * @return True if the search was interrupted early.
	 */
	public boolean forEachBottomUp(TreeVisitor<T> nodeVisitor){
		for(Tree<T> child : children){
			if(child.forEachBottomUp(nodeVisitor)){
				return true;
			}
		}
		
		return nodeVisitor.visitNode(this);
	}
	
	/**
	 * Gets the data that is stored at this tree node.
	 * @return The data stored at this tree node.
	 */
	public T getData(){
		return data;
	}
	
	/**
	 * Adds a new child node to this tree node.
	 * @param node The node to add as a child.
	 * @throws IllegalArgumentException When the given node
	 *         already has a parent node.
	 */
	public void addChild(Tree<T> node) throws IllegalArgumentException{
		if(node.parent != null){
			throw new IllegalArgumentException("Node already has a parent.");
		}
		
		children.add(node);
		node.parent = this;
	}
	
	/**
	 * Gets a list of child nodes for this tree node.
	 * @return The child nodes of this tree node.
	 */
	public List<Tree<T>> getChildren(){
		return children;
	}
	
	/**
	 * Gets the depth this node is at in the tree. Where
	 * a depth of 0 indicates the root node of the tree.
	 * @return The depth of this node in the tree.
	 */
	public int getDepth(){
		return parent == null ? 0 : (1 + parent.getDepth());
	}
	
	/**
	 * Returns a stream over all tree nodes in the sub tree
	 * rooted at this tree node.
	 * @return A stream over all nodes in the sub tree
	 *         rooted at this tree node.
	 */
	public Stream<Tree<T>> stream(){
		return Stream.concat(Stream.of(this), children.stream().flatMap(Tree::stream));
	}
	
	/**
	 * Checks if this node is the root node of the tree.
	 * @return True if this node is the root node of the tree.
	 */
	public boolean isRoot(){
		return parent == null;
	}
	
	/**
	 * Gets the parent node for this tree node.
	 * @return The parent node for this tree node or
	 *         <code>null</code> if this node is the root node.
	 */
	public Tree<T> getParent(){
		return parent;
	}
	
	/**
	 * Checks if this tree node is a leaf node.
	 * @return True if this node is a leaf node.
	 */
	public boolean isLeaf(){
		return children.isEmpty();
	}
	
	/**
	 * Constructs a structurally identical copy of this tree using
	 * the given function to set the data stored at each tree node.
	 * @param <N> The data type for the new tree.
	 * @param map The function to use to convert the data stored in this
	 *        tree to the data to store at the new tree.
	 * @return The newly created tree.
	 */
	public <N> Tree<N> cloneStructure(Function<T, N> map){
		Tree<N> root = new Tree<N>(map.apply(data));
		children.forEach(c->root.addChild(c.cloneStructure(map)));
		return root;
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * Note that nodes are iterated in such a way that all children
	 * of a node are always returned before their parent.
	 */
	@Override
	public Iterator<Tree<T>> iterator(){
		if(children.isEmpty()){
			return new Iterator<Tree<T>>(){
				/**
				 * True while the parent has not been returned yet.
				 */
				private boolean next = true;

				@Override
				public boolean hasNext(){
					return next;
				}

				@Override
				public Tree<T> next(){
					if(next){
						next = false;
						return Tree.this;
					}else{
						throw new NoSuchElementException();
					}
				}
			};
		}else{
			return new Iterator<Tree<T>>(){
				/**
				 * Current child being iterated.
				 */
				private int idx = 1;
				/**
				 * Iterator for the current child.
				 */
				private Iterator<Tree<T>> current = children.get(0).iterator();
				/**
				 * True while the parent has not been returned yet.
				 */
				private boolean next = true;

				@Override
				public boolean hasNext(){
					return next;
				}

				@Override
				public Tree<T> next(){
					if(current.hasNext()){
						return current.next();
					}else if(idx < children.size()){
						current = children.get(idx++).iterator();
						return current.next();
					}else if(next){
						next = false;
						return Tree.this;
					}else{
						throw new NoSuchElementException();
					}
				}
			};
		}
	}
	
	/**
	 * Interface for tree traversal implementations.
	 * @author Roan
	 * @param <T> The tree data type.
	 */
	@FunctionalInterface
	public static abstract interface TreeVisitor<T>{
		
		/**
		 * Called when the visitor arrives at a new node.
		 * @param node The node visited.
		 * @return True if this search should be stopped.
		 */
		public abstract boolean visitNode(Tree<T> node);
	}
}
