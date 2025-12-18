#!/bin/bash

# name:port
SERVICES=(
  "naming-server:8761"
  "config-server:8888"
  "user-service:9000"
  "wallet-service:8000"
  "transaction-service:8082"
  "gateway:8080"
)

echo "Checking service status..."
echo "--------------------------"

for svc in "${SERVICES[@]}"; do
  NAME="${svc%%:*}"
  PORT="${svc##*:}"

  PID=$(netstat -ano | grep LISTENING | grep ":$PORT " | awk '{print $NF}' | head -n 1)

  if [ -n "$PID" ]; then
    echo "✅ $NAME is running (PID=$PID, Port=$PORT)"
  else
    echo "❌ $NAME is NOT running (Port=$PORT)"
  fi
done
