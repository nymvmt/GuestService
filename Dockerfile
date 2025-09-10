# Build stage
FROM eclipse-temurin:17-jdk AS builder
WORKDIR /app
COPY . .
RUN ./gradlew clean build -x test

# Runtime stage
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar

# 포트 명시적 설정
EXPOSE 8083

# JVM 옵션 추가
ENTRYPOINT ["java", "-Dserver.port=8083", "-jar", "/app.jar"]