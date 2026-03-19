#!/bin/bash

# ==============================================================================
# 옵션 및 변수 설정
# ==============================================================================
PROJECT_DIR="$(cd "$(dirname "$0")/.." && pwd)" # donttaz-BE 디렉토리
# 기존 인프라망의 Nginx 컨테이너 명
NGINX_CONTAINER="nginx"
MAX_RETRIES=40
RETRY_INTERVAL=3
API_HEALTH_URL="/actuator/health"
AI_HEALTH_URL="/api/health" # FastAPI 헬스 체크 경로 (임시. 실제 경로로 수정 필요)

cd $PROJECT_DIR

# ==============================================================================
# 현재 활성화된 환경(Blue/Green) 확인
# ==============================================================================
# 1. Nginx의 설정을 보고 판단
CURRENT_ENV=""
if docker exec $NGINX_CONTAINER grep -q "api-blue.inc" /etc/nginx/conf.d/service-env.inc 2>/dev/null; then
    CURRENT_ENV="blue"
elif docker exec $NGINX_CONTAINER grep -q "api-green.inc" /etc/nginx/conf.d/service-env.inc 2>/dev/null; then
    CURRENT_ENV="green"
fi

# 2. Nginx 설정으로 확인 불가 시, 실제 실행 중인 컨테이너 확인
if [ -z "$CURRENT_ENV" ]; then
    if docker ps --format '{{.Names}}' | grep -q "donttaz-api-blue"; then
        CURRENT_ENV="blue"
    elif docker ps --format '{{.Names}}' | grep -q "donttaz-api-green"; then
        CURRENT_ENV="green"
    else
        # 둘 다 없으면 초기 상태이므로 green이 활성 상태였다고 가정 (최초 배포 시 blue를 띄움)
        CURRENT_ENV="green"
    fi
fi

if [ "$CURRENT_ENV" = "blue" ]; then
    TARGET_ENV="green"
    API_PORT=8082
    AI_PORT=8002
else
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
# 프로젝트 이름(-p)을 명시하여 Blue/Green 환경이 서로 간섭(down 시 다른 색상 삭제)하지 않도록 격리함
docker-compose -p donttaz-${TARGET_ENV} -f docker-compose.${TARGET_ENV}.yml up -d --build

# ==============================================================================
# Health Check (API & AI)
# ==============================================================================
# Spring API Health Check
echo "⏳ [$TARGET_ENV] Spring Boot API 기동 대기 중..."
API_IS_READY=false
for i in $(seq 1 $MAX_RETRIES); do
    STATUS_CODE=$(curl -s -o /dev/null -w "%{http_code}" http://donttaz-api-${TARGET_ENV}:8080${API_HEALTH_URL})
    if [ "${STATUS_CODE:-000}" -eq 200 ]; then
        echo "✅ [$TARGET_ENV] Spring Boot API 정상 기동 확인 (200 OK)"
        API_IS_READY=true
        break
    fi
    echo "  - 아직 준비되지 않음 (상태 코드: $STATUS_CODE). $RETRY_INTERVAL초 후 재시도... ($i/$MAX_RETRIES)"
    sleep $RETRY_INTERVAL
done

AI_IS_READY=true # AI 보류

if [ "$API_IS_READY" = false ] || [ "$AI_IS_READY" = false ]; then
    echo "❌ [$TARGET_ENV] 헬스 체크 실패. 배포를 중단하고 새로 띄운 컨테이너를 종료합니다."
    echo "🔍 에러 원인 파악을 위해 멈춘 컨테이너의 로그(최근 100줄)를 확인합니다."
    docker logs donttaz-api-${TARGET_ENV} --tail 100
    docker-compose -p donttaz-${TARGET_ENV} -f docker-compose.${TARGET_ENV}.yml down
    exit 1
fi

# ==============================================================================
# Nginx 트래픽 스위칭 (Reload)
# ==============================================================================
echo "🔀 Nginx 라우팅 경로를 [$TARGET_ENV] 로 변경합니다..."

# 최신 라우팅 파일을 Nginx 컨테이너 내부로 복사
docker cp ./nginx/api-blue.inc $NGINX_CONTAINER:/etc/nginx/conf.d/api-blue.inc
docker cp ./nginx/api-green.inc $NGINX_CONTAINER:/etc/nginx/conf.d/api-green.inc

# Nginx 에 사용될 프로파일 변경
docker exec $NGINX_CONTAINER sh -c "echo 'include /etc/nginx/conf.d/api-${TARGET_ENV}.inc;' > /etc/nginx/conf.d/service-env.inc"

echo "🔄 Nginx 리로드 (Docker)..."
docker exec $NGINX_CONTAINER nginx -s reload

if [ $? -eq 0 ]; then
    echo "🎉 Nginx 리로드 성공! 무중단 배포 완료."
else
    echo "💥 Nginx 리로드 실패! 설정을 롤백합니다."
    docker exec $NGINX_CONTAINER sh -c "echo 'include /etc/nginx/conf.d/api-${CURRENT_ENV}.inc;' > /etc/nginx/conf.d/service-env.inc"
    docker exec $NGINX_CONTAINER nginx -s reload
    docker-compose -p donttaz-${TARGET_ENV} -f docker-compose.${TARGET_ENV}.yml down
    exit 1
fi

# ==============================================================================
# 이전 환경 (Current) 종료 (Down)
# ==============================================================================
# 배포 완료 후 5초 대기 (안정화)
sleep 5
echo "🛑 이전 [$CURRENT_ENV] 환경 컨테이너 종료..."
docker-compose -p donttaz-${CURRENT_ENV} -f docker-compose.${CURRENT_ENV}.yml down

echo "✨ 모든 배포 파이프라인이 성공적으로 종료되었습니다."
