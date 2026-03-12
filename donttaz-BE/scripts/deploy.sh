#!/bin/bash

# ==============================================================================
# 옵션 및 변수 설정
# ==============================================================================
PROJECT_DIR="$(cd "$(dirname "$0")/.." && pwd)" # donttaz-BE 디렉토리
# Nginx 설정 파일 경로 (프로젝트 내부 경로를 사용하여 Jenkins 컨테이너에서도 접근 가능하게 함)
NGINX_CONF_PATH="$PROJECT_DIR/nginx/service-env.inc"
MAX_RETRIES=20
RETRY_INTERVAL=3
API_HEALTH_URL="/actuator/health"
AI_HEALTH_URL="/api/health" # FastAPI 헬스 체크 경로 (임시. 실제 경로로 수정 필요)

cd $PROJECT_DIR

# ==============================================================================
# 현재 활성화된 환경(Blue/Green) 확인
# ==============================================================================
# Nginx의 대상(symbolic link 혹은 include된 파일 내용)을 보고 판단
CURRENT_ENV=""

if grep -q "api-blue.inc" $NGINX_CONF_PATH 2>/dev/null; then
    CURRENT_ENV="blue"
    TARGET_ENV="green"
    API_PORT=8082
    AI_PORT=8002
else
    # 파일이 없거나 green이라고 가정
    CURRENT_ENV="green"
    TARGET_ENV="blue"
    API_PORT=8081
    AI_PORT=8001
fi

echo "================================================="
echo "🔄 현재 트래픽 환경: $CURRENT_ENV"
echo "🚀 배포 대상 환경: $TARGET_ENV"
echo "================================================="

# ==============================================================================
# Target 환경의 도커 컨테이너 띄우기 (Up)
# ==============================================================================
echo "📦 [$TARGET_ENV] 환경 도커 이미지 빌드 및 실행 중..."
docker-compose -f docker-compose.${TARGET_ENV}.yml up -d --build

# ==============================================================================
# Health Check (API & AI)
# ==============================================================================
# Spring API Health Check
echo "⏳ [$TARGET_ENV] Spring Boot API 기동 대기 중..."
API_IS_READY=false
for i in $(seq 1 $MAX_RETRIES); do
    STATUS_CODE=$(docker exec donttaz-api-$TARGET_ENV curl -s -o /dev/null -w "%{http_code}" http://127.0.0.1:8080$API_HEALTH_URL)
    if [ "$STATUS_CODE" -eq 200 ]; then
        echo "✅ [$TARGET_ENV] Spring Boot API 정상 기동 확인 (200 OK)"
        API_IS_READY=true
        break
    fi
    echo "  - 아직 준비되지 않음 (상태 코드: $STATUS_CODE). $RETRY_INTERVAL초 후 재시도... ($i/$MAX_RETRIES)"
    sleep $RETRY_INTERVAL
done

# FastAPI AI Health Check (선택적)
# echo "⏳ [$TARGET_ENV] FastAPI AI 기동 대기 중... (단자: $AI_PORT)"
# AI_IS_READY=false
# for i in $(seq 1 $MAX_RETRIES); do
#     STATUS_CODE=$(curl -s -o /dev/null -w "%{http_code}" http://127.0.0.1:$AI_PORT$AI_HEALTH_URL)
#     if [ "$STATUS_CODE" -eq 200 ]; then
#         echo "✅ [$TARGET_ENV] FastAPI AI 정상 기동 확인 (200 OK)"
#         AI_IS_READY=true
#         break
#     fi
#     echo "  - 아직 준비되지 않음 (상태 코드: $STATUS_CODE). $RETRY_INTERVAL초 후 재시도... ($i/$MAX_RETRIES)"
#     sleep $RETRY_INTERVAL
# done
AI_IS_READY=true # 일단 AI는 무조건 패스하는 것으로 임시 설정

if [ "$API_IS_READY" = false ] || [ "$AI_IS_READY" = false ]; then
    echo "❌ [$TARGET_ENV] 헬스 체크 실패. 배포를 중단하고 새로 띄운 컨테이너를 종료합니다."
    docker-compose -f docker-compose.${TARGET_ENV}.yml down
    exit 1
fi

# ==============================================================================
# Nginx 트래픽 스위칭 (Reload)
# ==============================================================================
echo "🔀 Nginx 라우팅 경로를 [$TARGET_ENV] 로 변경합니다..."

# 프로젝트 내부의 service-env.inc를 업데이트합니다.
echo "include /etc/nginx/conf.d/api-${TARGET_ENV}.inc;" > $NGINX_CONF_PATH

echo "🔄 Nginx 리로드 (Docker)..."
docker exec donttaz-nginx nginx -s reload

if [ $? -eq 0 ]; then
    echo "🎉 Nginx 리로드 성공! 무중단 배포 완료."
else
    echo "💥 Nginx 리로드 실패! 설정을 롤백합니다."
    echo "include /etc/nginx/conf.d/api-${CURRENT_ENV}.inc;" > $NGINX_CONF_PATH
    docker exec donttaz-nginx nginx -s reload
    docker-compose -f docker-compose.${TARGET_ENV}.yml down
    exit 1
fi

# ==============================================================================
# 이전 환경 (Current) 종료 (Down)
# ==============================================================================
# 시스템 리로드 후 구 버전 컨테이너는 더 이상 트래픽을 받지 않으므로 내립니다.
echo "🛑 이전 [$CURRENT_ENV] 환경 컨테이너 종료..."
docker-compose -f docker-compose.${CURRENT_ENV}.yml down

echo "✨ 모든 배포 파이프라인이 성공적으로 종료되었습니다."
