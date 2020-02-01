
# Scala Fs2 Kafka

## With Docker

1. Run Docker Compose file:
    `docker-compose up -d`

2. Your IP:
    * mac: `ifconfig | sed -En 's/127.0.0.1//;s/.*inet (addr:)?(([0-9]*\.){3}[0-9]*).*/\2/p'`
    * linux: `hostname -i | awk '{print $1}'`

3. List Topics:
    `docker exec -it my_scala_kafka bash`
    `/opt/kafka/bin/kafka-topics.sh --list --zookeeper {IP}:2181`
   
4. Create Consumer:
    `docker exec -it my_scala_kafka bash`
    `/opt/kafka/bin/kafka-console-consumer.sh -bootstrap-server {IP}:9092 --topic fs2.topic --from-beginning`
    
5. Run Consumer App:
    * `ConsumerKafka`: Reactor Consumer Kafka.

6. Run Publisher App:
    * `PublisherKafka`: Reactor Publisher Kafka.

## Dowload Apache Kafka

1. Dowload Apache Kafka.

2. Run Apache **Zookeeper**:
    `./{KAFKA_PATH}/bin/zookeeper-server-start.sh ./config/zookeeper.properties`

3. Run **Apache Kafka**:
    `./{KAFKA_PATH}/bin/kafka-server-start.sh ./config/server.properties`
    
4. Create `fs2.topic`:
    * With a Single partions:
        `./{KAFKA_PATH}/bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic fs2.topic`
    * With multiple partitions:
        `./{KAFKA_PATH}/bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 3 --topic fs2.topic`
 
5. List Topics:
    `./{KAFKA_PATH}/bin/kafka-topics.sh --list --zookeeper localhost:2181`
   
6. Create Consumer:
    `./{KAFKA_PATH}/bin/kafka-console-consumer.sh -bootstrap-server localhost:9092 --topic fs2.topic --from-beginning`
   
7. Run Consumer App:
    * `ConsumerKafka`: Reactor Consumer Kafka.

8. Run Publisher App:
    * `PublisherKafka`: Reactor Publisher Kafka.