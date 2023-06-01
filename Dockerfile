# syntax=docker/dockerfile:1
FROM eclipse-temurin:17 AS compile
LABEL maintainer="roan@roanh.dev"
WORKDIR /gMark
ADD gMark/gradle/wrapper/ /gMark/gradle/wrapper/
ADD gMark/src/ /gMark/src/
ADD gMark/build.gradle /gMark/
ADD gMark/gradlew /gMark/
ADD gMark/settings.gradle /gMark/
RUN chmod -R 755 ./
RUN ./gradlew :cliJar

FROM eclipse-temurin:17
LABEL maintainer="roan@roanh.dev"
WORKDIR /gMark
COPY --from=compile /gMark/build/libs/gMark-cli.jar ./gMark.jar
ENTRYPOINT ["java", "-jar", "gMark.jar"]