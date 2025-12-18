#!/bin/bash

# Load env globally
set -a
source .env
set +a

BASE_DIR=$(pwd)

start_service () {
  local name=$1
  local dir=$2

  echo "Starting $name in new terminal..."

  mintty bash -lc "
    cd \"$BASE_DIR/$dir\" &&
    echo \"Loaded env for $name\" &&
    mvn spring-boot:run
  "
}

start_service "naming-server" "naming-server"
sleep 15

start_service "config-server" "config-server/config-server"
sleep 15

start_service "user-service" "user-service"
sleep 15

start_service "wallet-service" "wallet-service"
sleep 15

start_service "transaction-service" "transaction-service"
sleep 15

start_service "gateway" "gate-way"
