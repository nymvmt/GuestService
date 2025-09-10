# Build stage
FROM eclipse-temurin:17-jdk AS builder
WORKDIR /app
COPY . .
RUN ./gradlew clean build -x test

# Runtime stage  
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar

# 디버깅을 위한 정보 출력
RUN ls -la /app/
RUN ls -la /app.jar || echo "app.jar not found"

# 포트 명시적 설정
EXPOSE 8083

# 디버깅용 ENTRYPOINT
ENTRYPOINT ["sh", "-c", "echo 'Current directory:' && pwd && echo 'Files in /app:' && ls -la /app/ && echo 'Checking app.jar:' && ls -la app.jar && echo 'Starting application...' && java -Dserver.port=8083 -jar app.jar"]