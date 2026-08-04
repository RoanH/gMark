/*
 * gMark: A domain- and query language-independent query workload generator and query language utility library.
 * Copyright (C) 2021  Roan Hofland (roan@roanh.dev).  All rights reserved.
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
 * along with gMark.  If not, see <http://www.gnu.org/licenses/>.
 */
package dev.roanh.cpqindex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import dev.roanh.cpqindex.Index.Block;
import dev.roanh.gmark.ast.OperationType;
import dev.roanh.gmark.ast.QueryTree;
import dev.roanh.gmark.lang.cpq.CPQ;
import dev.roanh.gmark.type.schema.Predicate;

/**
 * Evaluates CPQs using label-to-block and block-to-path mappings.
 * @author Roan
 */
final class PathIndexEvaluator{
	/**
	 * First input operand for a binary operation.
	 */
	private static final int FIRST = 0;
	/**
	 * Second input operand for a binary operation.
	 */
	private static final int SECOND = 1;
	/**
	 * Empty block result.
	 */
	private static final int[] EMPTY_BLOCKS = new int[0];
	/**
	 * Empty pair result.
	 */
	private static final long[] EMPTY_PAIRS = new long[0];
	/**
	 * Maximum label-sequence length represented directly by a block lookup.
	 */
	private final int maxPathLength;
	/**
	 * Final-layer blocks represented by primitive path arrays.
	 */
	private final BlockData[] blocks;
	/**
	 * Mapping from label sequences to sorted local block IDs.
	 */
	private final Map<List<Predicate>, int[]> labelToBlocks;
	/**
	 * Weak cache of compiled plans for immutable CPQ objects.
	 */
	private final Map<CPQ, QueryPlan> plans = Collections.synchronizedMap(new WeakHashMap<CPQ, QueryPlan>());

	/**
	 * Constructs an evaluator for the given final-layer partition blocks.
	 * @param sourceBlocks The blocks to evaluate queries against.
	 * @param maxPathLength The maximum indexed label-sequence length.
	 */
	public PathIndexEvaluator(List<Block> sourceBlocks, int maxPathLength){
		this.maxPathLength = maxPathLength;
		blocks = new BlockData[sourceBlocks.size()];
		Map<List<Predicate>, IntArrayBuilder> mappings = new HashMap<List<Predicate>, IntArrayBuilder>();

		for(int blockId = 0; blockId < sourceBlocks.size(); blockId++){
			Block block = sourceBlocks.get(blockId);
			long[] paths = new long[block.getPathCount()];
			for(int i = 0; i < paths.length; i++){
				Pair pair = block.getPaths().get(i);
				paths[i] = encode(pair.getSource(), pair.getTarget());
			}
			Arrays.sort(paths);
			blocks[blockId] = new BlockData(unique(paths), block.isLoop());

			for(LabelSequence sequence : block.getLabels()){
				List<Predicate> labels = List.copyOf(Arrays.asList(sequence.getLabels()));
				mappings.computeIfAbsent(labels, ignored->new IntArrayBuilder()).add(blockId);
			}
		}

		labelToBlocks = new HashMap<List<Predicate>, int[]>(mappings.size());
		mappings.forEach((labels, blockIds)->labelToBlocks.put(labels, blockIds.toSortedUniqueArray()));
	}

	/**
	 * Evaluates the given CPQ.
	 * @param cpq The query to evaluate.
	 * @return The matched source-target pairs in their natural order.
	 */
	public List<Pair> query(CPQ cpq){
		long[] pairs = materialise(evaluate(plan(cpq)));
		List<Pair> result = new ArrayList<Pair>(pairs.length);
		for(long pair : pairs){
			result.add(new Pair(source(pair), target(pair)));
		}
		return Collections.unmodifiableList(result);
	}

	/**
	 * Computes the result cardinality for the given CPQ.
	 * @param cpq The query to evaluate.
	 * @return The number of matched source-target pairs.
	 */
	public long computeResultCardinality(CPQ cpq){
		QueryResult result = evaluate(plan(cpq));
		if(result instanceof BlockResult blockResult){
			long count = 0;
			for(int block : blockResult.blocks()){
				count += blocks[block].paths().length;
			}
			return count;
		}else if(result instanceof PairResult pairResult){
			return pairResult.pairs().length;
		}else{
			throw new IllegalStateException("Cannot compute the cardinality of an unconstrained identity relation.");
		}
	}

	/**
	 * Gets or compiles the immutable evaluation plan for a CPQ.
	 * @param cpq The query to compile.
	 * @return The compiled query plan.
	 */
	private QueryPlan plan(CPQ cpq){
		synchronized(plans){
			return plans.computeIfAbsent(cpq, query->compile(query.toAbstractSyntaxTree()));
		}
	}

	/**
	 * Compiles a query AST to primitive evaluation operations.
	 * @param query The query AST to compile.
	 * @return The compiled query plan.
	 */
	private QueryPlan compile(QueryTree query){
		List<Predicate> labels = getLabelSequence(query);
		if(labels != null && labels.size() <= maxPathLength){
			return new BlockPlan(labelToBlocks.getOrDefault(labels, EMPTY_BLOCKS));
		}

		return switch(query.getOperation()){
		case CONCATENATION -> compileConcatenation(query);
		case IDENTITY -> IdentityPlan.INSTANCE;
		case INTERSECTION -> compileIntersection(query);
		default -> throw new IllegalArgumentException("Unsupported operation in CPQ: " + query.getOperation());
		};
	}

	/**
	 * Compiles concatenation and removes identity operands.
	 * @param query The concatenation AST.
	 * @return The compiled query plan.
	 */
	private QueryPlan compileConcatenation(QueryTree query){
		QueryPlan first = compile(query.getOperand(FIRST));
		QueryPlan second = compile(query.getOperand(SECOND));
		if(first instanceof IdentityPlan){
			return second;
		}else if(second instanceof IdentityPlan){
			return first;
		}else{
			return new ConcatPlan(first, second);
		}
	}

	/**
	 * Compiles intersection and retains identity as a specialised filter.
	 * @param query The intersection AST.
	 * @return The compiled query plan.
	 */
	private QueryPlan compileIntersection(QueryTree query){
		QueryPlan first = compile(query.getOperand(FIRST));
		QueryPlan second = compile(query.getOperand(SECOND));
		if(first instanceof IdentityPlan){
			return new IdentityFilterPlan(second);
		}else if(second instanceof IdentityPlan){
			return new IdentityFilterPlan(first);
		}else{
			return new IntersectPlan(first, second);
		}
	}

	/**
	 * Extracts a label sequence when the entire subtree consists of labels and
	 * concatenations.
	 * @param query The query subtree to inspect.
	 * @return The label sequence, or {@code null} when the subtree is not a path.
	 */
	private List<Predicate> getLabelSequence(QueryTree query){
		if(query.getOperation() == OperationType.EDGE){
			return List.of(query.getEdgeAtom().getLabel());
		}else if(query.getOperation() == OperationType.CONCATENATION){
			List<Predicate> first = getLabelSequence(query.getOperand(FIRST));
			List<Predicate> second = getLabelSequence(query.getOperand(SECOND));
			if(first != null && second != null){
				List<Predicate> labels = new ArrayList<Predicate>(first.size() + second.size());
				labels.addAll(first);
				labels.addAll(second);
				return List.copyOf(labels);
			}
		}

		return null;
	}

	/**
	 * Evaluates a compiled query while retaining block-based intermediate results.
	 * @param query The query plan to evaluate.
	 * @return The intermediate query result.
	 */
	private QueryResult evaluate(QueryPlan query){
		if(query instanceof BlockPlan blockPlan){
			return new BlockResult(blockPlan.blocks());
		}else if(query instanceof ConcatPlan concat){
			return concatenate(evaluate(concat.first()), evaluate(concat.second()));
		}else if(query instanceof IntersectPlan intersect){
			return intersect(evaluate(intersect.first()), evaluate(intersect.second()));
		}else if(query instanceof IdentityFilterPlan identity){
			return evaluateIdentity(identity.operand());
		}else{
			return IdentityResult.INSTANCE;
		}
	}

	/**
	 * Evaluates an identity filter, specialising concatenation as an identity join.
	 * @param query The input to the identity filter.
	 * @return The filtered query result.
	 */
	private QueryResult evaluateIdentity(QueryPlan query){
		if(query instanceof ConcatPlan concat){
			long[] first = materialise(evaluate(concat.first()));
			long[] second = materialise(evaluate(concat.second()));
			return new PairResult(join(first, second, true));
		}else{
			return selectIdentity(evaluate(query));
		}
	}

	/**
	 * Computes relational concatenation for two intermediate results.
	 * @param first The left relation.
	 * @param second The right relation.
	 * @return The concatenated relation.
	 */
	private QueryResult concatenate(QueryResult first, QueryResult second){
		if(first instanceof IdentityResult){
			return second;
		}else if(second instanceof IdentityResult){
			return first;
		}else{
			return new PairResult(join(materialise(first), materialise(second), false));
		}
	}

	/**
	 * Joins two sorted primitive pair relations.
	 * @param first The left relation sorted by source and target.
	 * @param second The right relation sorted by source and target.
	 * @param identity Whether to emit only loop results.
	 * @return The sorted, duplicate-free joined relation.
	 */
	private long[] join(long[] first, long[] second, boolean identity){
		if(first.length == 0 || second.length == 0){
			return EMPTY_PAIRS;
		}

		long[] byTarget = new long[first.length];
		for(int i = 0; i < first.length; i++){
			byTarget[i] = encode(target(first[i]), source(first[i]));
		}
		Arrays.sort(byTarget);

		LongArrayBuilder joined = new LongArrayBuilder();
		int left = 0;
		int right = 0;
		while(left < byTarget.length && right < second.length){
			int leftKey = source(byTarget[left]);
			int rightKey = source(second[right]);
			if(leftKey < rightKey){
				left = endOfSourceGroup(byTarget, left);
			}else if(leftKey > rightKey){
				right = endOfSourceGroup(second, right);
			}else{
				int leftEnd = endOfSourceGroup(byTarget, left);
				int rightEnd = endOfSourceGroup(second, right);
				if(identity){
					joinIdentityGroup(byTarget, left, leftEnd, second, right, rightEnd, joined);
				}else{
					for(int i = left; i < leftEnd; i++){
						int source = target(byTarget[i]);
						for(int j = right; j < rightEnd; j++){
							joined.add(encode(source, target(second[j])));
						}
					}
				}
				left = leftEnd;
				right = rightEnd;
			}
		}
		return joined.toSortedUniqueArray();
	}

	/**
	 * Joins a matching source group while only retaining loop results.
	 */
	private void joinIdentityGroup(long[] first, int firstStart, int firstEnd,
			long[] second, int secondStart, int secondEnd, LongArrayBuilder result){
		int left = firstStart;
		int right = secondStart;
		while(left < firstEnd && right < secondEnd){
			int source = target(first[left]);
			int target = target(second[right]);
			if(source < target){
				left++;
			}else if(source > target){
				right++;
			}else{
				result.add(encode(source, source));
				left++;
				right++;
			}
		}
	}

	/**
	 * Finds the end offset for pairs that share the same source.
	 */
	private int endOfSourceGroup(long[] pairs, int start){
		int value = source(pairs[start]);
		int end = start + 1;
		while(end < pairs.length && source(pairs[end]) == value){
			end++;
		}
		return end;
	}

	/**
	 * Computes relational intersection for two intermediate results.
	 * @param first The left relation.
	 * @param second The right relation.
	 * @return The intersected relation.
	 */
	private QueryResult intersect(QueryResult first, QueryResult second){
		if(first instanceof IdentityResult){
			return selectIdentity(second);
		}else if(second instanceof IdentityResult){
			return selectIdentity(first);
		}else if(first instanceof BlockResult left && second instanceof BlockResult right){
			return new BlockResult(intersect(left.blocks(), right.blocks()));
		}else{
			return new PairResult(intersect(materialise(first), materialise(second)));
		}
	}

	/**
	 * Intersects two sorted block ID arrays.
	 */
	private int[] intersect(int[] first, int[] second){
		IntArrayBuilder result = new IntArrayBuilder(Math.min(first.length, second.length));
		int left = 0;
		int right = 0;
		while(left < first.length && right < second.length){
			if(first[left] < second[right]){
				left++;
			}else if(first[left] > second[right]){
				right++;
			}else{
				result.add(first[left]);
				left++;
				right++;
			}
		}
		return result.toArray();
	}

	/**
	 * Intersects two sorted primitive pair arrays.
	 */
	private long[] intersect(long[] first, long[] second){
		LongArrayBuilder result = new LongArrayBuilder(Math.min(first.length, second.length));
		int left = 0;
		int right = 0;
		while(left < first.length && right < second.length){
			if(first[left] < second[right]){
				left++;
			}else if(first[left] > second[right]){
				right++;
			}else{
				result.add(first[left]);
				left++;
				right++;
			}
		}
		return result.toArray();
	}

	/**
	 * Selects loop pairs from the given relation.
	 * @param result The relation to filter.
	 * @return The loop pairs in the relation.
	 */
	private QueryResult selectIdentity(QueryResult result){
		if(result instanceof IdentityResult){
			return result;
		}else if(result instanceof BlockResult blockResult){
			IntArrayBuilder loops = new IntArrayBuilder(blockResult.blocks().length);
			for(int block : blockResult.blocks()){
				if(blocks[block].loop()){
					loops.add(block);
				}
			}
			return new BlockResult(loops.toArray());
		}else{
			long[] pairs = ((PairResult)result).pairs();
			LongArrayBuilder loops = new LongArrayBuilder();
			for(long pair : pairs){
				if(source(pair) == target(pair)){
					loops.add(pair);
				}
			}
			return new PairResult(loops.toArray());
		}
	}

	/**
	 * Materialises an intermediate result as a sorted primitive pair array.
	 * @param result The result to materialise.
	 * @return The sorted, duplicate-free relation.
	 */
	private long[] materialise(QueryResult result){
		if(result instanceof BlockResult blockResult){
			long count = 0;
			for(int block : blockResult.blocks()){
				count += blocks[block].paths().length;
			}
			if(count > Integer.MAX_VALUE){
				throw new IllegalStateException("Path-index result exceeds the maximum Java array size.");
			}

			long[] pairs = new long[(int)count];
			int offset = 0;
			for(int block : blockResult.blocks()){
				long[] paths = blocks[block].paths();
				System.arraycopy(paths, 0, pairs, offset, paths.length);
				offset += paths.length;
			}
			Arrays.sort(pairs);
			return unique(pairs);
		}else if(result instanceof PairResult pairResult){
			return pairResult.pairs();
		}else{
			throw new IllegalStateException("Cannot materialise an unconstrained identity relation.");
		}
	}

	/**
	 * Encodes a pair as a primitive long in natural pair order.
	 */
	private static long encode(int source, int target){
		return ((long)source << Integer.SIZE) | (target & 0xFFFFFFFFL);
	}

	/**
	 * Decodes the source component of a primitive pair.
	 */
	private static int source(long pair){
		return (int)(pair >> Integer.SIZE);
	}

	/**
	 * Decodes the target component of a primitive pair.
	 */
	private static int target(long pair){
		return (int)pair;
	}

	/**
	 * Removes duplicates from a sorted array.
	 */
	private static long[] unique(long[] values){
		if(values.length < 2){
			return values;
		}

		int size = 1;
		for(int i = 1; i < values.length; i++){
			if(values[i] != values[size - 1]){
				values[size++] = values[i];
			}
		}
		return size == values.length ? values : Arrays.copyOf(values, size);
	}

	/**
	 * Immutable primitive representation of an index block.
	 */
	private static record BlockData(long[] paths, boolean loop){
	}

	/**
	 * Compiled query plan.
	 */
	private sealed interface QueryPlan permits BlockPlan, ConcatPlan, IntersectPlan, IdentityFilterPlan, IdentityPlan{
	}

	private static record BlockPlan(int[] blocks) implements QueryPlan{
	}

	private static record ConcatPlan(QueryPlan first, QueryPlan second) implements QueryPlan{
	}

	private static record IntersectPlan(QueryPlan first, QueryPlan second) implements QueryPlan{
	}

	private static record IdentityFilterPlan(QueryPlan operand) implements QueryPlan{
	}

	private static enum IdentityPlan implements QueryPlan{
		INSTANCE;
	}

	/**
	 * Intermediate query evaluation result.
	 */
	private sealed interface QueryResult permits BlockResult, PairResult, IdentityResult{
	}

	private static record BlockResult(int[] blocks) implements QueryResult{
	}

	private static record PairResult(long[] pairs) implements QueryResult{
	}

	private static enum IdentityResult implements QueryResult{
		INSTANCE;
	}

	/**
	 * Growable primitive integer array.
	 */
	private static final class IntArrayBuilder{
		private int[] values;
		private int size;

		private IntArrayBuilder(){
			this(8);
		}

		private IntArrayBuilder(int capacity){
			values = new int[capacity];
		}

		private void add(int value){
			if(size == values.length){
				values = Arrays.copyOf(values, Math.max(8, size + (size >> 1) + 1));
			}
			values[size++] = value;
		}

		private int[] toArray(){
			return size == 0 ? EMPTY_BLOCKS : Arrays.copyOf(values, size);
		}

		private int[] toSortedUniqueArray(){
			Arrays.sort(values, 0, size);
			if(size > 1){
				int unique = 1;
				for(int i = 1; i < size; i++){
					if(values[i] != values[unique - 1]){
						values[unique++] = values[i];
					}
				}
				size = unique;
			}
			return toArray();
		}
	}

	/**
	 * Growable primitive long array.
	 */
	private static final class LongArrayBuilder{
		private long[] values;
		private int size;

		private LongArrayBuilder(){
			this(16);
		}

		private LongArrayBuilder(int capacity){
			values = new long[capacity];
		}

		private void add(long value){
			if(size == values.length){
				values = Arrays.copyOf(values, Math.max(16, size + (size >> 1) + 1));
			}
			values[size++] = value;
		}

		private long[] toArray(){
			return size == 0 ? EMPTY_PAIRS : Arrays.copyOf(values, size);
		}

		private long[] toSortedUniqueArray(){
			Arrays.sort(values, 0, size);
			if(size > 1){
				int unique = 1;
				for(int i = 1; i < size; i++){
					if(values[i] != values[unique - 1]){
						values[unique++] = values[i];
					}
				}
				size = unique;
			}
			return toArray();
		}
	}
}
