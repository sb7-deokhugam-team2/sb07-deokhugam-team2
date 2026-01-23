# 1. Base Image
FROM amazoncorretto:17-alpine-jdk

# 2. 작업 디렉토리 설정
WORKDIR /app

# [중요] 3. 한국 시간(KST) 설정
# Alpine 리눅스는 기본적으로 timezone 패키지가 없어서 설치해야 합니다.
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Seoul /etc/localtime && \
    echo "Asia/Seoul" > /etc/timezone

# 4. 빌드된 JAR 파일 복사
# (build/libs/*.jar 패턴은 plain jar와 섞일 수 있어 구체적인 이름을 지정하거나 주의가 필요합니다)
COPY build/libs/*.jar app.jar

# 5. 실행 명령어
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-Duser.timezone=Asia/Seoul", "-jar", "app.jar"]