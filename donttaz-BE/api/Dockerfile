FROM openjdk:17-jdk-slim AS build
WORKDIR /app

# Gradle Wrapper 및 설정 파일 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# 소스 코드 복사 (src 폴더)
COPY src src

# 실행 권한 부여 및 빌드 (테스트 제외)
RUN chmod +x ./gradlew
RUN ./gradlew clean build -x test

# 실행 스테이지
FROM openjdk:17-jdk-slim
WORKDIR /app

# 빌드 스테이지에서 생성된 jar 파일 복사
COPY --from=build /app/build/libs/*.jar app.jar

# Spring Boot 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]
