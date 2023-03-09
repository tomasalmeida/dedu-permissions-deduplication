#!/usr/bin/env bash

docker-compose up -d

# Wait zookeeper-1 is UP
ZOOKEEPER_STATUS=""
while [[ $ZOOKEEPER_STATUS != "imok" ]]; do
  echo "Waiting zookeeper UP..."
  sleep 1
  ZOOKEEPER_STATUS=$(echo ruok | docker-compose exec zookeeper nc localhost 2181)
done
echo "Zookeeper ready!!"

# Wait brokers is UP
FOUND=''
while [[ $FOUND != "yes" ]]; do
  echo "Waiting kafka UP..."
  sleep 1
  FOUND=$(docker-compose exec zookeeper zookeeper-shell zookeeper get /brokers/ids/1 &>/dev/null && echo 'yes')
done
echo "Kafka ready!!"

bash setACLs.sh