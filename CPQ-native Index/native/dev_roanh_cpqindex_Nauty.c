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
#include <dev_roanh_cpqindex_Nauty.h>
#include <core.h>

static TLS_ATTR int* labels;
static TLS_ATTR int* ptn;
static TLS_ATTR int* orbits;
static TLS_ATTR SG_DECL(inputGraph);
static TLS_ATTR SG_DECL(canon);

/**
 * Computes and returns the canonical form of the given colored graph using
 * the sparse version of nauty.
 * @param The JNI environment.
 * @param Calling class.
 * @param adj The input graph in adjacency list format, n arrays with
 *        each the indices of the neighbors of the n-th vertex.
 * @param colors The array containing raw color information data. Contains vertex
 *        indices in blocks of the same color with the start of a block of the same
 *        color being indicated by a negated value. All vertex indices are also always
 *        one higher than their actual index in the graph.
 * @return The relabeling function that can be used to constructed the canonical graph.
 *         The returned array has the same size as there were vertices in the graph. For
 *         each index the former index of that vertex is indicated. For example if at index
 *         0 the value 4 is stored, then this means that in the input graph vertex 0 was
 *         labeled as vertex 4.
 */
JNIEXPORT jintArray JNICALL Java_dev_roanh_cpqindex_Nauty_computeCanonSparse(JNIEnv* env, jclass obj, jobjectArray adj, jintArray colors){
	//construct input graph
	constructSparseGraph(env, &adj, &inputGraph);

	//set nauty settings
	static DEFAULTOPTIONS_SPARSEDIGRAPH(options);
	statsblk stats;
	options.getcanon = TRUE;
	options.defaultptn = FALSE;

	//allocated data structures
	int n = inputGraph.nv;
	DYNALLSTAT(int, labels, labels_sz);
	DYNALLSTAT(int, ptn, ptn_sz);
	DYNALLSTAT(int, orbits, orbits_sz);
	DYNALLOC1(int, labels, labels_sz, n, "jni canon sparse");
	DYNALLOC1(int, ptn, ptn_sz, n, "jni canon sparse");
	DYNALLOC1(int, orbits, orbits_sz, n, "jni canon sparse");

	//initialise the coloring of the graph
	parseColoring(env, n, &colors, labels, ptn);

	//compute canonical form and labeling
	sparsenauty(&inputGraph, labels, ptn, orbits, &options, &stats, &canon);

	//check for errors
	if(stats.errstatus != 0){
		return NULL;
	}

	//copy canonical labeling
	jintArray result = (*env)->NewIntArray(env, n);

	jint data[n];
	for(int i = 0; i < n; i++){
		data[i] = labels[i];
	}
	(*env)->SetIntArrayRegion(env, result, 0, n, data);

	//return the labeling
	return result;
}
