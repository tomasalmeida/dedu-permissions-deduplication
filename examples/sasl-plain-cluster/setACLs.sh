#!/usr/bin/env bash


# create topics
docker-compose exec kafka1 kafka-topics --bootstrap-server kafka1:19091 --command-config /etc/kafka/secrets/clients/kafka-user.properties \
   --create --topic topic-test11
docker-compose exec kafka1 kafka-topics --bootstrap-server kafka1:19091 --command-config /etc/kafka/secrets/clients/kafka-user.properties \
   --create --topic topic-test12
docker-compose exec kafka1 kafka-topics --bootstrap-server kafka1:19091 --command-config /etc/kafka/secrets/clients/kafka-user.properties \
   --create --topic topic-test13
docker-compose exec kafka1 kafka-topics --bootstrap-server kafka1:19091 --command-config /etc/kafka/secrets/clients/kafka-user.properties \
   --create --topic topic-test14
docker-compose exec kafka1 kafka-topics --bootstrap-server kafka1:19091 --command-config /etc/kafka/secrets/clients/kafka-user.properties \
   --create --topic topic-test15
docker-compose exec kafka1 kafka-topics --bootstrap-server kafka1:19091 --command-config /etc/kafka/secrets/clients/kafka-user.properties \
   --create --topic topic-testDeleted
docker-compose exec kafka1 kafka-topics --bootstrap-server kafka1:19091 --command-config /etc/kafka/secrets/clients/kafka-user.properties \
   --create --topic topic-testPrefixValid

# create permissions to all topic test11-15
docker-compose exec kafka1 kafka-acls --bootstrap-server kafka1:19091 --command-config /etc/kafka/secrets/clients/kafka-user.properties \
  --add --consumer --producer --allow-principal "User:alice" --group '*' \
  --topic topic-test11 --topic topic-test12 --topic topic-test13 --topic topic-test14 --topic topic-test15 --topic topic-testDeleted \
  --topic topic-testPrefixValid
docker-compose exec kafka1 kafka-acls --bootstrap-server kafka1:19091 --command-config /etc/kafka/secrets/clients/kafka-user.properties \
  --add --consumer --producer --allow-principal "User:bob" --group '*' \
  --topic topic-test11 --topic topic-test12 --topic topic-test13 --topic topic-test14 --topic topic-test15 --topic topic-testDeleted \
  --topic topic-testPrefixValid

# set a redundant prefix permission
docker-compose exec kafka1 kafka-acls --bootstrap-server kafka1:19091 --command-config /etc/kafka/secrets/clients/kafka-user.properties \
  --add --operation READ --allow-principal "User:alice" --group '*' \
  --topic topic-test1  --resource-pattern-type prefixed

# set a deleted prefix permission
docker-compose exec kafka1 kafka-acls --bootstrap-server kafka1:19091 --command-config /etc/kafka/secrets/clients/kafka-user.properties \
  --add --operation READ --allow-principal "User:alice" --group '*' \
  --topic topic-testDeleted  --topic topic-testPrefixValid --topic topic-testPrefix \
  --resource-pattern-type prefixed


# show permissions
docker-compose exec kafka1 kafka-acls --bootstrap-server kafka1:19091 --command-config /etc/kafka/secrets/clients/kafka-user.properties \
  --list --topic topic-test --resource-pattern-type match

echo "deleting topic-testDeleted"
docker-compose exec kafka1 kafka-topics --bootstrap-server kafka1:19091 --command-config /etc/kafka/secrets/clients/kafka-user.properties \
  --delete --topic topic-testDeleted