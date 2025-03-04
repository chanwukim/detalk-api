#!/bin/bash

# 전달받은 LOGSTASH 서버 ip, port 변수 할당
LOGSTASH_HOST=$1
LOGSTASH_PORT=$2

# 현재 포트 파일 경로
PORT_FILE="/home/ubuntu/current_port.txt"
# nginx 설정 파일 경로
NGINX_CONF="/etc/nginx/conf.d/api.detalk.net.conf"

# 현재 포트 읽기 (파일이 없으면 기본값 8080)
if [ -f "$PORT_FILE" ]; then
  CURRENT_PORT=$(cat "$PORT_FILE")
else
  CURRENT_PORT=8080
fi

# 포트번호 변경
if [ "$CURRENT_PORT" = "8080" ]; then
  NEW_PORT=8081
else
  NEW_PORT=8080
fi

echo "Current port: $CURRENT_PORT, New port: $NEW_PORT"

# 새 JAR 파일 실행
nohup java -jar /home/ubuntu/api-0.0.1-SNAPSHOT.jar \
  --server.port=$NEW_PORT \
  --spring.config.location=/home/ubuntu/application-prod.yaml \
  -DLOGSTASH_HOST=$LOGSTASH_HOST -DLOGSTASH_PORT=$LOGSTASH_PORT > /home/ubuntu/app-$NEW_PORT.log 2>&1 &


# 헬스 체크 (5초 대기 후 최대 10번 시도)
sleep 5
attempts=0
max_attempts=10
while [ $attempts -lt $max_attempts ]; do
  if curl -f http://localhost:$NEW_PORT/api/health >/dev/null 2>&1; then
    echo "Health check passed for port $NEW_PORT"
    break
  fi
  attempts=$((attempts + 1))
  echo "Health check failed, attempt $attempts of $max_attempts"
  sleep 5
done

if [ $attempts -eq $max_attempts ]; then
  echo "Health check failed after $max_attempts attempts, aborting deployment"
  pkill -f "java -jar.*$NEW_PORT"
  exit 1
fi

# 현재 포트 파일 업데이트
echo "$NEW_PORT" > "$PORT_FILE"

# Nginx 설정 파일에서 $proxy_port 값 수정
sudo sed -i "s/set \$proxy_port [0-1]*808[0-1];/set \$proxy_port $NEW_PORT;/" "$NGINX_CONF"

# Nginx 설정 테스트 및 재실행
sudo nginx -t && sudo nginx -s reload

# 이전 JAR 프로세스 종료
pkill -f "java -jar.*$CURRENT_PORT"
echo "Deployment successful: Switched to port $NEW_PORT"