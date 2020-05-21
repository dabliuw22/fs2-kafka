package com.leysoft

import cats.effect.ExitCode
import com.leysoft.adapters.config._
import com.leysoft.adapters.KafkaMessagePublisher
import com.leysoft.domain.{Message, MessageEvent, Metadata}
import com.leysoft.adapters.serde.JsonSerde
import fs2.kafka._
import fs2.Stream
import monix.eval.{Task, TaskApp}
import org.apache.kafka.clients.producer.ProducerConfig

object ProducerApp extends TaskApp {

  override def run(args: List[String]): Task[ExitCode] =
    config[Task]
      .map(conf => settings(conf.kafka.producer))
      .use { settings =>
        producerResource[Task]
          .using(settings)
          .use { producer =>
            for {

              publisher <- KafkaMessagePublisher.make[Task](producer, settings)
              _ <- Stream("Fs2", "Cats", "Kafka")
                    .map(
                      MessageEvent(_,
                                   Metadata(topic = "fs2.topic",
                                            key = "fs2.key"))
                    )
                    .flatMap(publisher.publish)
                    .compile
                    .drain
            } yield ExitCode.Success
          }
      }

  private def keySerializer: Serializer[Task, String] = Serializer[Task, String]

  private def valueSerializer: Serializer[Task, Message] =
    Serializer.delegate[Task, Message](JsonSerde())

  private def settings(
    config: KafkaProducerConfiguration
  ): ProducerSettings[Task, String, Message] =
    ProducerSettings(
      keySerializer = keySerializer,
      valueSerializer = valueSerializer
    ).withBootstrapServers(config.server.value)
      .withClientId(config.clientId.value)
      .withBatchSize(config.batchSize.value)
      .withLinger(config.linger)
      .withEnableIdempotence(config.idempotence)
      .withMaxInFlightRequestsPerConnection(config.requestsPerConnection.value)
      .withRetries(config.retries.value)
      .withAcks(Acks.All)
      .withProperties(
        (ProducerConfig.COMPRESSION_TYPE_CONFIG, config.compression.value)
      )
}
