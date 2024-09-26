# gMark [![](https://img.shields.io/github/release/RoanH/gMark.svg)](https://github.com/RoanH/gMark/releases)
gMark is a domain- and query language-independent query workload generator, as well as a general utility library for working with the CPQ (conjunctive path query) and RPQ (regular path query) query languages. This project was originally started as a rewrite of the original version of gMark available on GitHub at [gbagan/gmark](https://github.com/gbagan/gmark), with as goal to make gMark easier to extend and better documented. However, presently the focus of the project has shifted primarily towards query languages, notably CPQ. Graph generation is currently out of scope for this project, though full feature parity for query generation is still planned. Presently, most of the features available for RPQs in the original version of gMark are available for CPQs in this version, with the exception of some output formats. However, the utilities available within gMark for working with query languages in general are much more extensive than those available in the original version of gMark.

## Documentation & Research
The current state of the repository is the result of several research projects, each of these research items can be consulted for more information on a specific component in gMark:

- [Indexing Conjunctive Path Queries for Accelerated Query Evaluation](https://thesis.roanh.dev/), this is my master's thesis on constructing a CPQ-native Graph Database Index. This document currently contains the most extensive and detailed write-up of how CPQs are structured, and contains the specification for the algorithms in gMark for CPQ Query Graph Computation, Query Homomorphism testing, CPQ Core Computation, and [various other utility algorithms](https://github.com/RoanH/gMark/releases/tag/v1.2). The reference implementation for the CPQ-native Index itself can be found at [RoanH/CPQ-native-index](https://github.com/RoanH/CPQ-native-index).
- [Graph Database & Query Evaluation Terminology](https://research.roanh.dev/Graph%20Database%20&%20Query%20Evaluation%20Terminology%20v1.3.pdf), this report focuses on bridging the gap between query languages and query evaluation. All of the database operations implemented in gMark are described in detail in this report, as well as the construction of RPQ and CPQ queries, and AST creation.
- [CPQ Keys: a survey of graph canonization algorithms](https://research.roanh.dev/cpqkeys/CPQ%20Keys%20v1.1.pdf), the main purpose of this literature survey was to find suitable algorithms to use for CPQ core canonization. Within gMark the CPQ API was implemented for this purpose, including CPQ parsing, the initial Query Graph construction, and random CPQ generation. More details about the project can be found on its [site](https://cpqkeys.roanh.dev/) and in its repository at [RoanH/CPQKeys](https://github.com/RoanH/CPQKeys).
- [Conjunctive Path Query Generation for Benchmarking](https://research.roanh.dev/Conjunctive%20Path%20Query%20Generation%20for%20Benchmarking%20v2.8.pdf), this report was the original motivation for this gMark rewrite and contains details on the CPQ workload generation algorithms and data structures. The current GUI for gMark was also written primarily with the use case in this reporting mind.
- [gMark: Schema-Driven Generation of Graphs and Queries](https://arxiv.org/abs/1511.08386), this is the paper for the original version of gMark which details the motivation behind all the original design choices.
- [Language-aware Indexing for Conjunctive Path Queries](https://arxiv.org/abs/2003.03079), this is the first paper that introduced the CPQ query language under its current name.

The javadoc documentation for this repository can be found at: [gmark.docs.roanh.dev](https://gmark.docs.roanh.dev/)

## Getting started with gMark
To support a wide variety of of use cases gMark is a available in a number of different formats. 

- [As a standalone executable with both a graphical and command line interface](#executable-download)
- [As a docker image](#docker-image-)
- [As a maven artifact](#maven-artifact-)

### Command line usage
When using gMark on the command line the following arguments are supported:

```
usage: gmark [-c <file>] [-f] [-g <size>] [-h] [-o <folder>] [-s <syntax>] [-w <file>]
 -c,--config <file>     The workload and graph configuration file
 -f,--force             Overwrite existing files if present
 -h,--help              Prints this help text
 -o,--output <folder>   The folder to write the generated output to
 -s,--syntax <syntax>   The concrete syntax(es) to output
 -w,--workload <file>   Triggers workload generation, a previously generated input workload can
                        optionally be provided to generate concrete syntaxes for instead
```

For example, a workload of queries in SQL format can be generated using:

```sh
gmark -c config.xml -o ./output -s sql -w
```

An example configuration XML file can be found both [in this repository](gMark/client/example.xml) and in the graphical interface of the standalone executable. The example RPQ workload configuration files included in the original gMark repository are also compatible and can be found [in the use-cases folder](https://github.com/gbagan/gmark/tree/master/use-cases).

### Executable download
gMark is available as a standalone portable executable that has both a graphical interface and a command line interface. The graphical interface will only be launched when no command line arguments are passed. This version of gMark requires Java 17 or higher to run.
   
- [Windows executable download](https://github.com/RoanH/gMark/releases/download/v1.2/gMark-v1.2.exe)    
- [Runnable Java archive (JAR) download](https://github.com/RoanH/gMark/releases/download/v1.2/gMark-v1.2.jar)

All releases: [releases](https://github.com/RoanH/gMark/releases)    
GitHub repository: [RoanH/gMark](https://github.com/RoanH/gMark)

#### Command line usage of the standalone executable
The following commands show how to generate a workload of queries in SQL format using the standalone executable.

##### Windows executable
```bat
./gMark.exe -c config.xml -o ./output -s sql -w
```

##### Runnable Java archive
```sh
java -jar gMark.jar -c config.xml -o ./output -s sql -w
```

### Docker image [![](https://img.shields.io/docker/v/roanh/gmark?sort=semver)](https://hub.docker.com/r/roanh/gmark)
gMark is available as a [docker image](https://hub.docker.com/r/roanh/gmark) on Docker Hub. This means that you can obtain the image using the following command:

```sh
docker pull roanh/gmark:latest
```

Using the image then works much the same as the regular command line version of gMark. For example, we can generate the example workload of queries in SQL format using the following command:

```sh
docker run --rm -v "$PWD/data:/data" roanh/gmark:latest -c /data/config.xml -o /data/queries -s sql -w
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
	implementation 'dev.roanh.gmark:gmark:1.2'
}
```

##### Maven
```xml
<dependency>
	<groupId>dev.roanh.gmark</groupId>
	<artifactId>gmark</artifactId>
	<version>1.2</version>
</dependency>
```

## Query Language API


## Development of gMark
This repository contain an [Eclipse](https://www.eclipse.org/) & [Gradle](https://gradle.org/) project with [Util](https://github.com/RoanH/Util) and [Apache Commons CLI](https://commons.apache.org/proper/commons-cli/introduction.html) as the only dependencies. Development work can be done using the Eclipse IDE or using any other Gradle compatible IDE. Continuous integration will check that all source files use Unix style line endings (LF) and that all functions and fields have valid documentation. Unit testing is employed to test core functionality, CI will also check for regressions using these tests. A hosted version of the javadoc for gMark can be found at [gmark.docs.roanh.dev](https://gmark.docs.roanh.dev/). Compiling the runnable Java archive (JAR) release of gMark using Gradle can be done using the following command in the `gMark` directory:

```sh
./gradlew client:shadowJar
```

After which the generated JAR can be found in the `build/libs` directory. On windows `./gradlew.bat` should be used instead of `./gradlew`.

## History
Project development started: 25th of September, 2021.