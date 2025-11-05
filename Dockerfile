FROM maven:3.8-openjdk-17 as builder

WORKDIR /app

COPY pom.xml .
COPY .mvn .

RUN mvn -B dependency:go-offline

COPY src /app/src

RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine

RUN apk add --no-cache curl

WORKDIR /app

COPY --from=builder /app/target/smart-home-automation-system-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]

HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
  CMD ["sh", "-c", "wget -q -O- http://localhost:8080/actuator/health || exit 1"]
