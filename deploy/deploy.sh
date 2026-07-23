#!/bin/bash
# Billage 백엔드 배포 스크립트
#
#   ./deploy/deploy.sh
#
# 로컬에서 jar 를 빌드해 EC2 로 올리고 컨테이너를 재시작합니다.
# 서버(t3.small)에서 Gradle 빌드를 돌리면 메모리 부족으로 죽기 때문에 빌드는 로컬에서 합니다.
#
# 사전 준비
#   - billage-key.pem 이 저장소 루트에 있을 것
#   - 서버 /opt/billage/.env.prod 에 비밀값이 채워져 있을 것
set -euo pipefail

HOST="${BILLAGE_HOST:-api.billage.site}"
SSH_KEY="${BILLAGE_KEY:-billage-key.pem}"
REMOTE_DIR="/opt/billage"

cd "$(dirname "$0")/.."

echo "▶ 1/4  jar 빌드"
./gradlew bootJar -x test --no-daemon -q
JAR=$(ls -t build/libs/*.jar | grep -v plain | head -1)
echo "   $JAR ($(du -h "$JAR" | cut -f1))"

echo "▶ 2/4  파일 전송 → $HOST"
scp -o StrictHostKeyChecking=no -i "$SSH_KEY" \
    "$JAR" "ubuntu@$HOST:$REMOTE_DIR/app.jar"
scp -o StrictHostKeyChecking=no -i "$SSH_KEY" \
    deploy/docker-compose.prod.yml deploy/Caddyfile "ubuntu@$HOST:$REMOTE_DIR/"

echo "▶ 3/4  컨테이너 재시작"
ssh -o StrictHostKeyChecking=no -i "$SSH_KEY" "ubuntu@$HOST" \
    "cd $REMOTE_DIR && docker compose -f docker-compose.prod.yml --env-file .env.prod up -d --remove-orphans"

echo "▶ 4/4  헬스체크"
for i in $(seq 1 30); do
    if curl -sf "https://$HOST/api/health" > /dev/null 2>&1; then
        echo "   ✅ https://$HOST/api/health 정상"
        exit 0
    fi
    sleep 5
done

echo "   ❌ 헬스체크 실패. 로그를 확인하세요:"
echo "      ssh -i $SSH_KEY ubuntu@$HOST 'cd $REMOTE_DIR && docker compose -f docker-compose.prod.yml logs --tail=80 app'"
exit 1
