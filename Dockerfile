FROM maven:3.9.4-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src

RUN mvn clean package -DskipTests

FROM tomee:10.0.0-M3-jre17-plume

WORKDIR /usr/local/tomee

COPY --from=build /app/target/*.war ./webapps/ROOT.war
COPY --from=build /app/target/justbin ./webapps/ROOT

EXPOSE 8080

CMD ["catalina.sh", "run"]