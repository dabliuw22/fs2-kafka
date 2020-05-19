package com.leysoft

import cats.effect.ExitCode
import com.leysoft.serde.JsonSerde
import com.leysoft.domain.{Message, MessageEvent, Metadata}
import fs2.kafka._
import fs2.Stream
import monix.eval.{Task, TaskApp}

import scala.concurrent.duration._

object ProducerApp extends TaskApp {
  import cats.syntax.functor._ // for as()

  val keySerializer: Serializer[Task, String] = Serializer[Task, String]
  val serializer: Serializer[Task, Message] =
    Serializer.delegate[Task, Message](JsonSerde())
  val producerSettings: ProducerSettings[Task, String, Message] =
    ProducerSettings(
      keySerializer = keySerializer,
      valueSerializer = serializer
    ).withBootstrapServers("localhost:9092")
      .withClientId("fs2.client")
      .withBatchSize(500)
      .withLinger(10 milliseconds)
      .withEnableIdempotence(true)
      .withRetries(3)
      .withAcks(Acks.All)

  override def run(args: List[String]): Task[ExitCode] = {
    Stream("Fs2", "Cats", "Kafka")
      .map(MessageEvent(_, Metadata(topic = "fs2.topic")))
      .covary[Task]
      .map { message =>
        val record = ProducerRecord("fs2.topic", "fs2.key", message)
        ProducerRecords.one(record)
      }
      .through(produce(producerSettings))
      .compile
      .drain
      .as(ExitCode.Success)
  }
}
