#!/usr/bin/env bash

WAREHOUSE_CONTAINER_NAME="pismo-warehouse"

docker restart ${WAREHOUSE_CONTAINER_NAME} 2>&1 1>/dev/null
