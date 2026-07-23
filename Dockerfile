# 빌드 단계 - 소스에서 jar 를 만듭니다.
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# 의존성 먼저 받아두면 소스만 바뀔 때 이 레이어를 재사용합니다.
COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon

COPY src ./src
RUN ./gradlew bootJar --no-daemon -x test

# 실행 단계 - JRE 만 담아 이미지 크기를 줄입니다.
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
ENV TZ=Asia/Seoul
ENV SPRING_PROFILES_ACTIVE=prod

ENTRYPOINT ["java", "-jar", "app.jar"]
