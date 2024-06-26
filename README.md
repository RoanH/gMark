# gMark [![](https://img.shields.io/github/release/RoanH/gMark.svg)](https://github.com/RoanH/gMark/releases)
gMark is a domain- and query language-independent graph instance and query workload generator. The original version of gMark is available on GitHub at [gbagan/gmark](https://github.com/gbagan/gmark). The version of gMark in this repository has as goal to rewrite gMark such that it is easier to extend and has better documented code. Currently the focus of the rewrite is on query generation, but the end goal is full feature parity with the original version of gMark. The rewrite has not reached that point yet and is notably still missing graph generation, RPQ (regular path query) based queries and several output formats. However, the current version does also offer some features not present in the original version of gMark, such as the ability to generate CPQ (conjunctive path query) based queries, various utilities for working with CPQs, and a graphical user interface for the program.

Documentation (javadoc) can be found at: [gmark.docs.roanh.dev](https://gmark.docs.roanh.dev/) more details on gMark itself can be found in the technical report [arxiv.org/abs/1511.08386](https://arxiv.org/abs/1511.08386). Details regarding the aforementioned queries containing CPQs can be found in my report titled [Conjunctive Path Query Generation for Benchmarking](https://research.roanh.dev/Conjunctive%20Path%20Query%20Generation%20for%20Benchmarking%20v2.8.pdf).

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
 -g,--graph <size>      Triggers graph generation, a graph size can be provided (overrides the ones
                        set in the configuration file)
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

## Development of gMark
This repository contain an [Eclipse](https://www.eclipse.org/) & [Gradle](https://gradle.org/) project with [Util](https://github.com/RoanH/Util) and [Apache Commons CLI](https://commons.apache.org/proper/commons-cli/introduction.html) as the only dependencies. Development work can be done using the Eclipse IDE or using any other Gradle compatible IDE. Continuous integration will check that all source files use Unix style line endings (LF) and that all functions and fields have valid documentation. Unit testing is employed to test core functionality, CI will also check for regressions using these tests. A hosted version of the javadoc for gMark can be found at [gmark.docs.roanh.dev](https://gmark.docs.roanh.dev/). Compiling the runnable Java archive (JAR) release of gMark using Gradle can be done using the following command in the `gMark` directory:

```sh
./gradlew client:shadowJar
```

After which the generated JAR can be found in the `build/libs` directory. On windows `./gradlew.bat` should be used instead of `./gradlew`.

## History
Project development started: 25th of September, 2021.