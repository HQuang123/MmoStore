FROM tomcat:10.1.20-jre17-temurin-jammy

WORKDIR /usr/local/tomcat

COPY  ./target/MMO-1.0-SNAPSHOT.war webapps/ROOT.war

EXPOSE 8080


