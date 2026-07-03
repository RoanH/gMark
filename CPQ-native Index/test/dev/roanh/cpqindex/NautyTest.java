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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

public class NautyTest{
	private static final int[][] TEST_GRAPH = new int[][]{
		new int[]{2, 3},
		new int[]{},
		new int[]{6},
		new int[]{7},
		new int[]{1},
		new int[]{1},
		new int[]{5},
		new int[]{4}
	};
	
	@Test
	public void fullCanon0(){
		assertArrayEquals(new int[]{4, 5, 6, 7, 2, 1, 0, 3}, Nauty.computeCanonSparse(new int[][]{
			new int[]{5},
			new int[]{4},
			new int[]{7, 6},
			new int[]{},
			new int[]{3},
			new int[]{3},
			new int[]{1},
			new int[]{0},
		}, new int[]{-5, -6, 7, -8, 1, 2, 3, -4}));
	}
	
	@Test
	public void fullCanon1(){
		assertArrayEquals(new int[]{4, 5, 7, 6, 2, 1, 0, 3}, Nauty.computeCanonSparse(new int[][]{
			new int[]{5},
			new int[]{4},
			new int[]{7, 6},
			new int[]{},
			new int[]{3},
			new int[]{3},
			new int[]{0},
			new int[]{1},
		}, new int[]{-5, -6, 7, -8, 1, 2, 3, -4}));
	}
	
	@Test
	public void fullCanon4(){
		int[][] g = new int[][]{
			new int[]{3},
			new int[]{6},
			new int[]{7, 5},
			new int[]{4},
			new int[]{},
			new int[]{0},
			new int[]{4},
			new int[]{1},
		};
		int[] c = new int[]{-3, -5, 6, -8, -7, -4, 1, -2};
		
		assertTrue(Arrays.deepEquals(TEST_GRAPH, relabel(g, Nauty.computeCanonSparse(g, c))));
	}
	
	@Test
	public void fullCanon5(){
		int[][] g = new int[][]{
			new int[]{3},
			new int[]{6},
			new int[]{7, 5},
			new int[]{4},
			new int[]{},
			new int[]{0},
			new int[]{4},
			new int[]{1},
		};
		int[] c = new int[]{-3, -5, 6, -8, -4, -7, 1, -2};
		
		assertTrue(Arrays.deepEquals(TEST_GRAPH, relabel(g, Nauty.computeCanonSparse(g, c))));
	}
	
	@Test
	public void fullCanon6(){
		int[][] g = new int[][]{
			new int[]{6},
			new int[]{3},
			new int[]{7, 5},
			new int[]{4},
			new int[]{},
			new int[]{0},
			new int[]{4},
			new int[]{1},
		};
		int[] c = new int[]{-3, -5, 6, -8, -4, -7, 1, -2};
		
		assertTrue(Arrays.deepEquals(TEST_GRAPH, relabel(g, Nauty.computeCanonSparse(g, c))));
	}
	
	@Test
	public void fullCanon(){
		int[][] g2 = new int[][]{
			new int[]{3},
			new int[]{6},
			new int[]{7, 5},
			new int[]{4},
			new int[]{},
			new int[]{0},
			new int[]{4},
			new int[]{1},
		};
		int[] c2 = new int[]{-3, -5, 6, -8, -4, -7, 1, -2};
		
		int[][] g3 = new int[][]{
			new int[]{6},
			new int[]{3},
			new int[]{7, 5},
			new int[]{4},
			new int[]{},
			new int[]{0},
			new int[]{4},
			new int[]{1},
		};
		int[] c3 = new int[]{-3, -5, 6, -8, -4, -7, 1, -2};
		
		assertTrue(Arrays.deepEquals(
			relabel(g3, Nauty.computeCanonSparse(g3, c3)),
			relabel(g2, Nauty.computeCanonSparse(g2, c2))
		));
	}
	
	private int[][] relabel(int[][] g, int[] lab){
		int[] inv = new int[lab.length];
 		for(int i = 0; i < lab.length; i++){
			inv[lab[i]] = i;
		}
 		
 		int[][] graph = new int[lab.length][];
		for(int i = 0; i < lab.length; i++){
			int[] row = g[lab[i]];
			graph[i] = new int[row.length];
			for(int j = 0; j < row.length; j++){
				graph[i][j] = inv[row[j]];
			}
			Arrays.sort(graph[i]);
		}
		
		return graph;
	}
}
