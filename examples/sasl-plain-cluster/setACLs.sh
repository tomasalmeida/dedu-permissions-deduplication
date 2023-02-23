#!/usr/bin/env bash
#docker-compose exec control-center bash -c 'export PRINCIPAL=User:c3 && control-center-set-acls /etc/kafka/secrets/clients/kafka-user.properties'

docker-compose exec kafka1 kafka-topics --bootstrap-server kafka1:19091 --command-config /etc/kafka/secrets/clients/kafka-user.properties \
   --create --topic topic-test1
docker-compose exec kafka1 kafka-topics --bootstrap-server kafka1:19091 --command-config /etc/kafka/secrets/clients/kafka-user.properties \
   --create --topic topic-test2
docker-compose exec kafka1 kafka-topics --bootstrap-server kafka1:19091 --command-config /etc/kafka/secrets/clients/kafka-user.properties \
   --create --topic topic-test3
docker-compose exec kafka1 kafka-topics --bootstrap-server kafka1:19091 --command-config /etc/kafka/secrets/clients/kafka-user.properties \
   --create --topic topic-test4
docker-compose exec kafka1 kafka-topics --bootstrap-server kafka1:19091 --command-config /etc/kafka/secrets/clients/kafka-user.properties \
   --create --topic topic-test5

docker-compose exec kafka1 kafka-acls --bootstrap-server kafka1:19091 --command-config /etc/kafka/secrets/clients/kafka-user.properties \
  --add --consumer --producer --allow-principal "User:alice" --group '*' \
  --topic topic-test1 --topic topic-test2 --topic topic-test3 --topic topic-test4 --topic topic-test5

docker-compose exec kafka1 kafka-acls --bootstrap-server kafka1:19091 --command-config /etc/kafka/secrets/clients/kafka-user.properties \
  --add --operation READ --allow-principal "User:alice" --group '*' \
  --topic topic-test  --resource-pattern-type prefixed

docker-compose exec kafka1 kafka-acls --bootstrap-server kafka1:19091 --command-config /etc/kafka/secrets/clients/kafka-user.properties \
  --list --topic topic-test1

docker-compose exec kafka1 kafka-acls --bootstrap-server kafka1:19091 --command-config /etc/kafka/secrets/clients/kafka-user.properties \
  --list --topic topic-test --resource-pattern-type match

docker-compose exec kafka1 kafka-topics --bootstrap-server kafka1:19091 --command-config /etc/kafka/secrets/clients/kafka-user.properties \
  --delete --topic topic-test1