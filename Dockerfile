# syntax=docker/dockerfile:1
FROM openjdk:8 AS compile
WORKDIR /gMark
ADD gMark/gradle/wrapper/* /gMark/gradle/wrapper/
ADD gMark/src/* /gMark/src/
ADD gMark/build.gradle /gMark/
ADD gMark/gradlew /gMark/
ADD gMark/settings.gradle /gMark/
RUN chmod -R 755 ./
RUN ./gradlew :cliJar

FROM openjdk:8
WORKDIR /gMark
COPY --from=compile /gMark/build/libs/gMark-cli-SNAPSHOT.jar ./gMark/gMark.jar
ENTRYPOINT ["java", "-jar", "gMark.jar"]

