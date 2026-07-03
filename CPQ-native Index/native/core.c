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
#include <core.h>

/**
 * Constructs a sparse graph from the given adjacency list
 * representation of a graph.
 * @param env JNI environment.
 * @param adj The input graph in adjacency list format, n arrays with
 *        each the indices of the neighbors of the n-th vertex.
 * @param graph A pointer to the sparse graph instance to populate.
 */
void constructSparseGraph(JNIEnv* env, jobjectArray* adj, sparsegraph* graph){
	jsize len = (*env)->GetArrayLength(env, *adj);
	nauty_check(WORDSIZE, SETWORDSNEEDED(len), len, NAUTYVERSIONID);
	graph->nv = len;

	graph->nde = 0;
	for(int i = 0; i < len; i++){
		graph->nde += (*env)->GetArrayLength(env, (jintArray)((*env)->GetObjectArrayElement(env, *adj, i)));
	}

	SG_ALLOC(*graph, len, graph->nde, "malloc");
	int offset = 0;
	for(int i = 0; i < len; i++){
		jintArray row = (jintArray)((*env)->GetObjectArrayElement(env, *adj, i));
		jsize rlen = (*env)->GetArrayLength(env, row);
		jint* elem = (*env)->GetIntArrayElements(env, row, 0);

		graph->v[i] = offset;
		graph->d[i] = rlen;

		for(int j = 0; j < rlen; j++){
			graph->e[offset] = elem[j];
			offset++;
		}

		(*env)->ReleaseIntArrayElements(env, row, elem, 0);
	}
}

/**
 * Constructs the graph coloring information arrays 'labels' and 'ptn'
 * from the given color data array.
 * @param env JNI environment.
 * @param len The number of vertices in the graph (and also the size of all the arrays).
 * @param colors The array containing raw color information data. Contains vertex
 *        indices in blocks of the same color with the start of a block of the same
 *        color being indicated by a negated value. All vertex indices are also always
 *        one higher than their actual index in the graph.
 * @param labels The array containing all the vertex indices for the color information,
 *        combined with the ptn array this describes the graph coloring.
 * @param ptn Array indicating at which indices in the labels array a new block of vertices
 *        with the same color starts. Start indices will have a value of 0 while
 *        all other indices will have a value of 1.
 */
void parseColoring(JNIEnv* env, int len, jintArray* colors, int* labels, int* ptn){
	jint* colorData = (*env)->GetIntArrayElements(env, *colors, 0);
	for(int i = 0; i < len; i++){
		if(colorData[i] < 0){
			labels[i] = -colorData[i] - 1;
			ptn[i] = 0;
		}else{
			labels[i] = colorData[i] - 1;
			ptn[i] = 1;
		}
	}
	(*env)->ReleaseIntArrayElements(env, *colors, colorData, 0);
}
