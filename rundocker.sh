#!/usr/bin/env bash

IMAGE_NAME="mgaldieri/pismo-warehouse:latest"
CONTAINER_NAME="pismo-warehouse"
SERVER_IP=localhost
SERVER_PORT=8000

export DOCKER_IP=${DOCKER_IP}
export WAREHOUSE_SERVER_PORT=${WAREHOUSE_SERVER_PORT}

if hash docker-machine 2>/dev/null; then
    docker-machine start default 2>&1 1>/dev/null
    docker-machine env default 2>&1 1>/dev/null
    SERVER_IP=`docker-machine ip default`
fi

if docker run -d --name ${CONTAINER_NAME} -p ${SERVER_PORT}:${SERVER_PORT} ${IMAGE_NAME} 2>&1 1>/dev/null; then
    echo "Servidor PISMO-WAREHOUSE rodando em ${SERVER_IP}:${SERVER_PORT}"
fi
