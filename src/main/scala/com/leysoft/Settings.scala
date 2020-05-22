package com.leysoft

import cats.effect.Effect
import com.leysoft.adapters.config.{KafkaConsumerConfiguration, KafkaProducerConfiguration}
import com.leysoft.adapters.serde.JsonSerde
import com.leysoft.domain.Message
import fs2.kafka._
import org.apache.kafka.clients.producer.ProducerConfig

object Settings {

  object Consumer {

    private def keyDeserializer[F[_]: Effect]: Deserializer[F, String] =
      Deserializer[F, String]

    private def valueDeserializer[F[_]: Effect]: Deserializer[F, Message] =
      Deserializer.delegate[F, Message](JsonSerde())

    def settings[F[_]: Effect](
      config: KafkaConsumerConfiguration
    ): ConsumerSettings[F, String, Message] = {
      println(config.groupId.value)
      ConsumerSettings(
        keyDeserializer = keyDeserializer,
        valueDeserializer = valueDeserializer
      ).withBootstrapServers(config.server.value)
        .withGroupId(config.groupId.value)
        .withEnableAutoCommit(config.autoCommit)
        .withAutoOffsetReset(AutoOffsetReset.Earliest)
        .withIsolationLevel(IsolationLevel.ReadCommitted)
    }
  }

  object Producer {

    private def keySerializer[F[_]: Effect]: Serializer[F, String] =
      Serializer[F, String]

    private def valueSerializer[F[_]: Effect]: Serializer[F, Message] =
      Serializer.delegate[F, Message](JsonSerde())

    def settings[F[_]: Effect](
      config: KafkaProducerConfiguration
    ): ProducerSettings[F, String, Message] =
      ProducerSettings(
        keySerializer = keySerializer,
        valueSerializer = valueSerializer
      ).withBootstrapServers(config.server.value)
        .withClientId(config.clientId.value)
        .withBatchSize(config.batchSize.value)
        .withLinger(config.linger)
        .withEnableIdempotence(config.idempotence)
        .withMaxInFlightRequestsPerConnection(
          config.requestsPerConnection.value
        )
        .withRetries(config.retries.value)
        .withAcks(Acks.All)
        .withProperties(
          (ProducerConfig.COMPRESSION_TYPE_CONFIG, config.compression.value)
        )
  }
}
