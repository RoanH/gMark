# QuickSilver
QuickSilver is a simple graph database that supports evaluating reachability queries written in the CPQ and RPQ query languages.

The two most important references for working with this codebase are the optimisations documented in [Optimising a Simple Graph Database](https://research.roanh.dev/Optimising%20a%20Simple%20Graph%20Database%20v1.1.pdf) and the [Graph Database & Query Evaluation Terminology](https://research.roanh.dev/Graph%20Database%20&%20Query%20Evaluation%20Terminology%20v1.3.pdf) reference document. Other references can be found at the end of this README under [Main Reference Material](#main-reference-material).

## High Level Overview
QuickSilver is divided into three source sets:

- The [core](QuickSilver/core/nl/group9/quicksilver) main source set contains the general high level logic to make the database run and the definitions of the concepts used and implemented by the database evaluator internals. You should **not** make any changes to the code in this source set.
- The [impl](QuickSilver/impl/nl/group9/quicksilver/impl) main source set contains the concrete database evaluator implementation which you should attempt to optimise at much as possible. The only real requirement for this source set is that an implementation of the interfaces in the [spec](QuickSilver/core/nl/group9/quicksilver/core/spec) package is provided and that the concrete evaluator and database graph implementation that should be used by the program are returned via the [Provider](QuickSilver/impl/nl/group9/quicksilver/impl/Provider.java) class. This source set also contains references to the optimisations listed in the optimisations document, optimisations listed in brackets are generally less applicable or things that can be implemented in multiple places. 
- The [test](QuickSilver/test/nl/group9/quicksilver) test source set contains a number of unit tests based on the datasets available in the [workload](QuickSilver/workload) folder. A small program is also provided in [BenchmarkRuns](QuickSilver/test/nl/group9/quicksilver/BenchmarkRuns.java) that directly runs all the workloads, feel free to edit this class. You can also add more of your own unit tests if you want, however, please do not remove any existing unit tests.

## Compiling
This repository contains an [Eclipse](https://www.eclipse.org/) & [Gradle](https://gradle.org/) project with [gMark](https://github.com/RoanH/gMark) and [Apache Commons CLI](https://commons.apache.org/proper/commons-cli/introduction.html) as the only dependencies. 

Compiling a runnable can be done using the following command in the [QuickSilver](QuickSilver) directory:

```sh
./gradlew shadowJar
```

Or on Windows:

```sh
.\gradlew.bat shadowJar
```

This will place the built JAR in the `build/libs` folder.

## Benchmarking
After building the application benchmark results for a single workload can be obtained by running:

```sh
java -jar QuickSilver.jar -g workload/syn/mini/graph.edge -w workload/syn/mini/cpq.query -o result.json
```

Here `-g` specifies the database graph file, `-w` the query workload (note that the filename indicates the query language), and the optional `-o` argument specifies the file to write the benchmark times to. The benchmark output contains four numbers:

- **Load Time**: The time it took to load the database graph into memory in nanoseconds.
- **Prep Time**: The time it took to prepare the evaluator in nanoseconds.
- **Eval Time**: The time it took to evaluate the query workload in nanoseconds.
- **Score**: A combined weighted score based on the three times.

For more details see the [BenchmarkResult](QuickSilver/core/nl/group9/quicksilver/core/data/BenchmarkResult.java) record.

## Main Reference Material

- [gMark](https://github.com/RoanH/gMark), the library used for some of the core database functionality (CPQ/RPQ/AST), other utilities from this library can also be used.
- [Graph Database & Query Evaluation Terminology](https://research.roanh.dev/Graph%20Database%20&%20Query%20Evaluation%20Terminology%20v1.3.pdf), the hand-out provided with basic definitions of all core terminology and concepts used.
- [Querying Graphs](https://perso.liris.cnrs.fr/angela.bonifati/pubs/book-Bonifati-et-al-18.pdf), this book is the main reference for graph specific database algorithms.
- [Database System Concepts](https://www.db-book.com/), this book is the main reference for non-graph specific database algorithms (note that we have one copy in the library).
- [Indexing Conjunctive Path Queries for Accelerated Query Evaluation](https://thesis.roanh.dev/), this is my master's thesis on constructing a CPQ-native Graph Database Index. This document currently contains the most extensive and detailed write-up of how CPQs are structured, and contains the specification for many of the algorithms implemented in gMark.

## History
Project development started: 14th of June, 2024.
