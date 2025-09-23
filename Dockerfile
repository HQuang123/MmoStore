#build stage
FROM maven:3.9-amazoncorretto-21-debian AS build
WORKDIR /app
COPY . .
RUN mvn install -DskipTests=true

#run stage
FROM amazoncorretto:21-alpine
COPY --from=build /app/target/MmoStore-0.0.1-SNAPSHOT.jar /run/MmoStore-0.0.1-SNAPSHOT.jar
EXPOSE 8080

ENTRYPOINT ["java","-jar","/run/MmoStore-0.0.1-SNAPSHOT.jar"]