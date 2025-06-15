# gMark [![](https://img.shields.io/github/release/RoanH/gMark.svg)](https://github.com/RoanH/gMark/releases)
gMark is a domain- and query language-independent query workload generator, as well as a general utility library for working with the CPQ (conjunctive path query) and RPQ (regular path query) query languages. gMark also includes a complete query evaluation pipeline for both of these query languages. This project was originally started as a rewrite of the original version of gMark available on GitHub at [gbagan/gmark](https://github.com/gbagan/gmark), with as goal to make gMark easier to extend and better documented. However, presently the focus of the project has shifted primarily towards query languages, notably CPQ. Graph generation is currently out of scope for this project, though full feature parity for query generation is still planned. Presently, most of the features available for RPQs in the original version of gMark are available for CPQs in this version, with the exception of some output formats. However, the utilities available within gMark for working with query languages in general are much more extensive than those available in the original version of gMark. In addition, this version of gMark also has a highly optimised evaluation pipeline for CPQ and RPQ queries.

## Documentation & Research
The current state of the repository is the result of several research projects, each of these research items can be consulted for more information on a specific component in gMark:

- [Indexing Conjunctive Path Queries for Accelerated Query Evaluation](https://thesis.roanh.dev/), this is my master's thesis on constructing a CPQ-native Graph Database Index. This document currently contains the most extensive and detailed write-up of how CPQs are structured, and contains the specification for the algorithms in gMark for CPQ Query Graph Computation, Query Homomorphism testing, CPQ Core Computation, and [various other utility algorithms](https://github.com/RoanH/gMark/releases/tag/v1.2). The reference implementation for the CPQ-native Index itself can be found at [RoanH/CPQ-native-index](https://github.com/RoanH/CPQ-native-index).
- [Graph Database & Query Evaluation Terminology](https://research.roanh.dev/Graph%20Database%20&%20Query%20Evaluation%20Terminology%20v1.3.pdf), this report focuses on bridging the gap between query languages and query evaluation. All of the database operations implemented in gMark are described in detail in this report, as well as the construction of RPQ and CPQ queries, and AST creation. More detailed information on this topic can be found in the [Querying Graphs](https://perso.liris.cnrs.fr/angela.bonifati/pubs/book-Bonifati-et-al-18.pdf) and [Database System Concepts](https://www.db-book.com/) books.
- [CPQ Keys: a survey of graph canonization algorithms](https://research.roanh.dev/cpqkeys/CPQ%20Keys%20v1.1.pdf), the main purpose of this literature survey was to find suitable algorithms to use for CPQ core canonization. Within gMark the CPQ API was implemented for this purpose, including CPQ parsing, the initial Query Graph construction, and random CPQ generation. More details about the project can be found on its [site](https://cpqkeys.roanh.dev/) and in its repository at [RoanH/CPQKeys](https://github.com/RoanH/CPQKeys).
- [Conjunctive Path Query Generation for Benchmarking](https://research.roanh.dev/Conjunctive%20Path%20Query%20Generation%20for%20Benchmarking%20v2.8.pdf), this report was the original motivation for this gMark rewrite and contains details on the CPQ workload generation algorithms and data structures. The current GUI for gMark was also written primarily with the use case in this report in mind.
- [gMark: Schema-Driven Generation of Graphs and Queries](https://arxiv.org/abs/1511.08386), this is the paper for the original version of gMark which details the motivation behind all the original design choices.
- [Language-aware Indexing for Conjunctive Path Queries](https://arxiv.org/abs/2003.03079), this is the first paper that introduced the CPQ query language under its current name.

The javadoc documentation for this repository can be found at: [gmark.docs.roanh.dev](https://gmark.docs.roanh.dev/)

## Getting started with gMark
To support a wide variety of of use cases gMark is a available in a number of different formats. 

- [As a standalone executable with both a graphical and command line interface](#executable-download)
- [As a docker image](#docker-image-)
- [As a maven artifact](#maven-artifact-)

### Command line usage
gMark can be used from the command line to either evaluate queries on a database graph, or to generate a workload of queries.

#### Evaluating Queries
When using gMark on the command line to evaluate queries the following arguments are supported.

```
usage: gmark evaluate [-f] [-g <data>] [-h] [-l <query language>] [-o <file>] [-q <query>] [-s
       <source>] [-t <target>] [-w <file>]
 -f,--force                       Overwrite the output file if present.
 -g,--graph <data>                The database graph file.
 -h,--help                        Prints this help text.
 -l,--language <query language>   The query language for the queries to execute (cpq or rpq).
 -o,--output <file>               The file to write the query output to.
 -q,--query <query>               The query to evaluate.
 -s,--source <source>             Optionally the bound source node for the query.
 -t,--target <target>             Optionally the bound target node for the query.
 -w,--workload <file>             The query workload to run, one query per line with format
                                  'source, query, target'.
```

The evaluator is intended to be used with either a single query to evaluate (`-s`/`-q`/`-t`) or with a complete workload of queries (`-w`). The database graph is expected to be provided in a simple text based graph format with on the first line the number of vertices, edges and labels, and a single edge definition following the `source target label` format on the remaining lines. Queries are expected to be either CPQs or RPQs and if provided as a workload file, a single query is allowed per line following the `source,query,target` format, if the source/target is not bound `*` can be provided instead. Finally, note that vertices and labels are represented by integers. Various example graphs and query workloads can be found in the [workload](gMark/test/workload) folder.

For example, a single CPQ query can be evaluated using:

```sh
gmark evaluate -l cpq -s 56 -q "a ◦ b" -t 5 -g ./graph.edge -o out.txt
```

Alternatively, an entire workload of queries can be evaluated using:

```sh
gmark evaluate -l cpq -w ./queries.cpq -g ./graph.edge -o out.txt
```

Note that only limited query evaluation output is written to the console, in particular, the result paths are only written to the provided output file if any.

#### Workload Generation
When using gMark on the command line for workload generation the following arguments are supported:

```
usage: gmark workload [-c <file>] [-f] [-h] [-o <folder>] [-s <syntax>]
 -c,--config <file>     The workload and graph configuration file.
 -f,--force             Overwrite existing files if present.
 -h,--help              Prints this help text.
 -o,--output <folder>   The folder to write the generated output to.
 -s,--syntax <syntax>   The concrete syntax(es) to output (sql and/or formal).
```

For example, a workload of queries in SQL format can be generated using:

```sh
gmark workload -c config.xml -o ./output -s sql
```

An example configuration XML file can be found both [in this repository](gMark/client/example.xml) and in the graphical interface of the standalone executable. The example RPQ workload configuration files included in the original gMark repository are also compatible and can be found [in the use-cases folder](https://github.com/gbagan/gmark/tree/master/use-cases).

### Executable download
gMark is available as a standalone portable executable that has both a graphical interface and a command line interface. The graphical interface will only be launched when no command line arguments are passed. This version of gMark requires Java 21 or higher to run.
   
- [Windows executable download](https://github.com/RoanH/gMark/releases/download/v2.0/gMark-v2.0.exe)    
- [Runnable Java archive (JAR) download](https://github.com/RoanH/gMark/releases/download/v2.0/gMark-v2.0.jar)

All releases: [releases](https://github.com/RoanH/gMark/releases)    
GitHub repository: [RoanH/gMark](https://github.com/RoanH/gMark)

#### Command line usage of the standalone executable
The following commands show how to generate a workload of queries in SQL format using the standalone executable. Note that more detailed command line usage instructions are given in [command line usage](#command-line-usage). 

##### Windows executable
```bat
./gMark.exe workload -c config.xml -o ./output -s sql
```

##### Runnable Java archive
```sh
java -jar gMark.jar workload -c config.xml -o ./output -s sql
```

### Docker image [![](https://img.shields.io/docker/v/roanh/gmark?sort=semver)](https://hub.docker.com/r/roanh/gmark)
gMark is available as a [docker image](https://hub.docker.com/r/roanh/gmark) on Docker Hub. This means that you can obtain the image using the following command:

```sh
docker pull roanh/gmark:latest
```

Using the image then works much the same as the regular [command line](#command-line-usage) version of gMark. For example, we can generate the example workload of queries in SQL format using the following command:

```sh
docker run --rm -v "$PWD/data:/data" roanh/gmark:latest workload -c /data/config.xml -o /data/queries -s sql
```

Note that we mount a local folder called `data` into the container to pass our configuration file and to retrieve the generated queries.

### Maven artifact [![Maven Central](https://img.shields.io/maven-central/v/dev.roanh.gmark/gmark)](https://mvnrepository.com/artifact/dev.roanh.gmark/gmark)
gMark is available on Maven central as [an artifact](https://mvnrepository.com/artifact/dev.roanh.gmark/gmark) so it can be included directly in another Java project using Gradle or Maven. This way it becomes possible to directly use all the implemented constructs and utilities. A hosted version of the javadoc for gMark can be found at [gmark.docs.roanh.dev](https://gmark.docs.roanh.dev/).

##### Gradle 
```groovy
repositories{
	mavenCentral()
}

dependencies{
	implementation 'dev.roanh.gmark:gmark:2.0'
}
```

##### Maven
```xml
<dependency>
	<groupId>dev.roanh.gmark</groupId>
	<artifactId>gmark</artifactId>
	<version>2.0</version>
</dependency>
```

## Query Language API
Most of the query language API is accessible directly via the [CPQ](https://gmark.docs.roanh.dev/dev/roanh/gmark/lang/cpq/CPQ.html) and [RPQ](https://gmark.docs.roanh.dev/dev/roanh/gmark/lang/rpq/RPQ.html) classes. For example, queries can be constructed using:

```java
Predicate a = new Predicate(0, "a");

CPQ query = CPQ.parse("a ∩ a");
CPQ query = CPQ.intersect(a, a);
CPQ query = CPQ.generateRandomCPQ(4, 1);

RPQ query = RPQ.parse("a ◦ a");
RPQ query = RPQ.disjunct(RPQ.concat(a, a), a);
RPQ query = RPQ.generateRandomRPQ(4, 1);
```

For CPQs query graphs and cores can be constructed using:

```java
CPQ query = ...;

QueryGraphCPQ graph = query.toQueryGraph();
QueryGraphCPQ core = query.toQueryGraph().computeCore();
QueryGraphCPQ core = query.computeCore();
```

Other notable utilities for CPQ and RPQ are:

```java
CPQ query = ...;

String sql = query.toSQL();
String formal = query.toFormalSyntax();
QueryTree ast = query.toAbstractSyntaxTree();
```

Note that CPQs and RPQs can also be constructed from an AST, which can sometimes be used to convert between the two query languages:

```java
RPQ rpq = RPQ.parse("a ◦ a");
CPQ cpq = CPQ.parse(rpq.toAbstractSyntaxTree());
```

All more general utilities can be found in the `dev.roanh.gmark.util` package.

## Development of gMark
This repository contain an [Eclipse](https://www.eclipse.org/) & [Gradle](https://gradle.org/) project with [Util](https://github.com/RoanH/Util) and [Apache Commons CLI](https://commons.apache.org/proper/commons-cli/introduction.html) as the only dependencies. Development work can be done using the Eclipse IDE or using any other Gradle compatible IDE. Continuous integration will check that all source files use Unix style line endings (LF) and that all functions and fields have valid documentation. Unit testing is employed to test core functionality, CI will also check for regressions using these tests. A hosted version of the javadoc for gMark can be found at [gmark.docs.roanh.dev](https://gmark.docs.roanh.dev/). Compiling the runnable Java archive (JAR) release of gMark using Gradle can be done using the following command in the `gMark` directory:

```sh
./gradlew client:shadowJar
```

After which the generated JAR can be found in the `build/libs` directory. On windows `./gradlew.bat` should be used instead of `./gradlew`.

## History
Project development started: 25th of September, 2021.