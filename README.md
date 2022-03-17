# gMark [![](https://img.shields.io/github/release/RoanH/gMark.svg)](https://github.com/RoanH/gMark/releases)
gMark is a domain- and query language-independent graph instance and query workload generator. The original version of gMark is available on GitHub at [gbagan/gmark](https://github.com/gbagan/gmark). The version of gMark in this repository has as goal to rewrite gMark such that it is easier to extend and has better documented code. Currently the focus of the rewrite is on query generation, but the end goal is full feature parity with the original version of gMark. The rewrite has not reached that point yet and is notably still missing graph generation, RPQ (regular path query) based queries and several output formats. The current version does however also offer some features not present in the original version of gMark, such as the ability to generate CPQ (conjunctive path query) based queries and a graphical user interface for the program.

Documentation (javadoc) can be found at: [gmark.docs.roanh.dev](https://gmark.docs.roanh.dev/) more details on gMark itself can be found in the technical report [arxiv.org/abs/1511.08386](https://arxiv.org/abs/1511.08386). Details regarding the aforementioned queries containing CPQs can be found at [https://research.roanh.dev/TODO](https://research.roanh.dev/TODO).

##cli or something maybe also link gmark technical report

## Getting started with gMark
To support a wide variety of of use cases gMark is a available in a number of different formats. 

- 

###
When using gMark on command line the following arguments are supported:

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

### Executable download
gMark is available as a standalone portable executable that has both a graphical interface and a command line interface. The graphical interface will only be launched when no command line arguments are passed.

_Requires Java 8 or higher_    
- [Windows executable](https://github.com/RoanH/gMark/releases/download/v1.0/gMark-v1.0.exe)    
- [Runnable Java Archive](https://github.com/RoanH/gMark/releases/download/v1.0/gMark-v1.0.jar)

All releases: [releases](https://github.com/RoanH/gMark/releases)    
GitHub repository: [RoanH/gMark](https://github.com/RoanH/gMark)

### Docker container
gMark is available as a docker image on Docker Hub.





### Maven artifact


required output:
- client jar
- client exe
- no deps jar for maven
- with deps jar for docker but without client



## Development
This repository contain an [Eclipse](https://www.eclipse.org/) & [Gradle](https://gradle.org/) project with [Util](https://github.com/RoanH/Util) and [Apache Commons CLI](https://commons.apache.org/proper/commons-cli/introduction.html) as the only dependencies. Development work can be done using the Eclipse IDE (already setup) or using any other Gradle compatible IDE (manual setup). CI will check that all source files use Unix style line endings (LF) and that all functions and fields have valid documentation. Unit testing is employed to test core functionality, CI will also check for regressions using these tests. A hosted version of the javadoc for gMark can be found at [gmark.docs.roanh.dev](https://gmark.docs.roanh.dev/).

## History
Project development started: 25th of September, 2021.



