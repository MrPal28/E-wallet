#!/bin/bash

SERVICES=(
  "naming-server:8761"
  "config-server:8888"
  "user-service:9000"
  "wallet-service:8000"
  "transaction-service:8082"
  "gateway:8080"
)

for svc in "${SERVICES[@]}"; do
  NAME="${svc%%:*}"
  PORT="${svc##*:}"

  PID=$(netstat -ano | grep LISTENING | grep ":$PORT " | awk '{print $NF}' | head -n 1)

  if [ -n "$PID" ]; then
    echo "Stopping $NAME (PID=$PID)"
    taskkill //PID "$PID" //F
  else
    echo "$NAME not running"
  fi
done
