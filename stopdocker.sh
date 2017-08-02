#!/usr/bin/env bash

CONTAINER_NAME="pismo-warehouse"

if docker stop ${CONTAINER_NAME} 2>&1 1>/dev/null; then
    docker rm ${CONTAINER_NAME} 2>&1 1>/dev/null

    echo "Servidor PISMO-WAREHOUSE encerrado"
fi

unset DOCKER_IP
unset WAREHOUSE_SERVER_PORT
