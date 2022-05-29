FROM openjdk:12.0.2
EXPOSE 8080

COPY ./build/libs/*.jar app.jar

ENTRYPOINT ["java","-jar","/app.jar"]
