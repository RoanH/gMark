# syntax=docker/dockerfile:1
ARG version=0.0

FROM eclipse-temurin:21 AS compile
LABEL maintainer="roan@roanh.dev"
ARG version
WORKDIR /gMark
ADD gMark/gradle/wrapper/ /gMark/gradle/wrapper/
ADD gMark/src/ /gMark/src/
ADD gMark/build.gradle /gMark/
ADD gMark/gradlew /gMark/
ADD gMark/settings.gradle /gMark/
ADD gMark/cli/src/ gMark/cli/src/
RUN chmod -R 755 ./
RUN ./gradlew -PrefName=v$version cli:shadowJar

FROM eclipse-temurin:21
LABEL maintainer="roan@roanh.dev"
ARG version
WORKDIR /gMark
COPY --from=compile /gMark/cli/build/libs/gMark-v$version.jar ./gMark.jar
ENTRYPOINT ["java", "-jar", "gMark.jar"]