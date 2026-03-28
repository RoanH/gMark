# syntax=docker/dockerfile:1
ARG version=0.0

FROM eclipse-temurin:25 AS compile
LABEL maintainer="roan@roanh.dev"
ARG version
WORKDIR /gMark
COPY gMark/ /gMark/
RUN chmod -R 755 ./
RUN ./gradlew -PrefName=v$version cli:shadowJar

FROM eclipse-temurin:25
LABEL maintainer="roan@roanh.dev"
ARG version
WORKDIR /gMark
COPY --from=compile /gMark/cli/build/libs/gMark-v$version.jar ./gMark.jar
ENTRYPOINT ["java", "-jar", "gMark.jar"]