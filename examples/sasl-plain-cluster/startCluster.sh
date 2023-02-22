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

docker-compose exec kafka1 kafka-topics --bootstrap-server kafka1:19091 --topic topic-test1 --create --command-config /etc/kafka/secrets/clients/kafka-user.properties
docker-compose exec kafka1 kafka-topics --bootstrap-server kafka1:19091 --topic topic-test2 --create --command-config /etc/kafka/secrets/clients/kafka-user.properties
docker-compose exec kafka1 kafka-topics --bootstrap-server kafka1:19091 --topic topic-test3 --create --command-config /etc/kafka/secrets/clients/kafka-user.properties
docker-compose exec kafka1 kafka-topics --bootstrap-server kafka1:19091 --topic topic-test4 --create --command-config /etc/kafka/secrets/clients/kafka-user.properties
docker-compose exec kafka1 kafka-topics --bootstrap-server kafka1:19091 --topic topic-test5 --create --command-config /etc/kafka/secrets/clients/kafka-user.properties
#
#docker-compose exec kafka1 kafka-acls --bootstrap-server kafka1:19091 --command-config /etc/kafka/secrets/clients/kafka-user.properties \
#  --add --consumer --producer --allow-principal "User:alice" --group '*' \
#  --topic topic-test1 --topic topic-test2 --topic topic-test3 --topic topic-test4 --topic topic-test5
#
#docker-compose exec kafka1 kafka-acls --bootstrap-server kafka1:19091 --command-config /etc/kafka/secrets/clients/kafka-user.properties \
#  --add --operation READ --allow-principal "User:alice" --group '*' \
#  --topic topic-test  --resource-pattern-type prefixed
#
##docker-compose exec kafka1 kafka-acls --bootstrap-server kafka1:19091 --command-config /etc/kafka/secrets/clients/kafka-user.properties \
#  --list --topic topic-test1
#
#docker-compose exec kafka1 kafka-acls --bootstrap-server kafka1:19091 --command-config /etc/kafka/secrets/clients/kafka-user.properties \
#  --list --topic topic-test --resource-pattern-type match

#docker-compose exec kafka1 kafka-topics --bootstrap-server kafka1:19091 --topic topic-test1 --delete --command-config /etc/kafka/secrets/clients/kafka-user.properties