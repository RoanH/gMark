# syntax=docker/dockerfile:1
ARG ref=SNAPSHOT

FROM eclipse-temurin:17 AS compile
LABEL maintainer="roan@roanh.dev"
ARG ref
WORKDIR /gMark
ADD gMark/gradle/wrapper/ /gMark/gradle/wrapper/
ADD gMark/src/ /gMark/src/
ADD gMark/build.gradle /gMark/
ADD gMark/gradlew /gMark/
ADD gMark/settings.gradle /gMark/
ADD gMark/cli/src/ gMark/cli/src/
RUN chmod -R 755 ./
RUN ./gradlew -PrefName=$ref cli:shadowJar

FROM eclipse-temurin:17
LABEL maintainer="roan@roanh.dev"
ARG ref
WORKDIR /gMark
COPY --from=compile /gMark/cli/build/libs/gMark-$ref.jar ./gMark.jar
ENTRYPOINT ["java", "-jar", "gMark.jar"]