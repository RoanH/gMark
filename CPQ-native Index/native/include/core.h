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
#ifndef include_dev_roanh_cpqindex_core
#define include_dev_roanh_cpqindex_core

#include <jni.h>
#include <nausparse.h>

/**
 * Constructs a sparse graph from the given adjacency list
 * representation of a graph.
 */
void constructSparseGraph(JNIEnv*, jobjectArray*, sparsegraph*);

/**
 * Constructs the graph coloring information arrays 'labels' and 'ptn'
 * from the given color data array.
 */
void parseColoring(JNIEnv*, int, jintArray*, int*, int*);

#endif
