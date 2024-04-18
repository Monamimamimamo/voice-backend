FROM openjdk:21-jdk-slim

COPY target/eureka-server-3.2.4.jar /app.jar

WORKDIR /app

CMD ["java", "--enable-preview", "-jar", "/app.jar"]
