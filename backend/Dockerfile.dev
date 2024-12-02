FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

COPY . .

RUN chmod +x ./gradlew

EXPOSE 8080

CMD ["./gradlew", "bootRun"]
