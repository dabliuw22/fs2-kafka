package com.leysoft

import cats.effect.ExitCode
import com.leysoft.adapters.KafkaMessagePublisher
import com.leysoft.domain.{Message, MessageEvent, Metadata}
import com.leysoft.serde.JsonSerde
import fs2.kafka._
import fs2.Stream
import monix.eval.{Task, TaskApp}
import org.apache.kafka.clients.producer.ProducerConfig

import scala.concurrent.duration._

object ProducerApp extends TaskApp {

  override def run(args: List[String]): Task[ExitCode] =
    producerResource[Task]
      .using(settings)
      .use { producer =>
        for {
          publisher <- KafkaMessagePublisher.make[Task](producer, settings)
          _ <- Stream("Fs2", "Cats", "Kafka")
                .map(
                  MessageEvent(_,
                               Metadata(topic = "fs2.topic", key = "fs2.key"))
                )
                .flatMap(publisher.publish)
                .compile
                .drain
        } yield ExitCode.Success
      }

  private def keySerializer: Serializer[Task, String] = Serializer[Task, String]

  private def valueSerializer: Serializer[Task, Message] =
    Serializer.delegate[Task, Message](JsonSerde())

  private def settings: ProducerSettings[Task, String, Message] =
    ProducerSettings(
      keySerializer = keySerializer,
      valueSerializer = valueSerializer
    ).withBootstrapServers("localhost:9092")
      .withClientId("fs2.client")
      .withBatchSize(500)
      .withLinger(10 milliseconds)
      .withEnableIdempotence(true)
      .withMaxInFlightRequestsPerConnection(3)
      .withRetries(3)
      .withAcks(Acks.All)
      .withProperties((ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy"))
}
