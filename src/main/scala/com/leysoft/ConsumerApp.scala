package com.leysoft

import cats.effect.{ExitCode, IO, IOApp}
import com.leysoft.adapters.{KafkaMessageSubscriber, MessageEventHandler, Subscription}
import com.leysoft.serde.JsonSerde
import com.leysoft.domain.{Message, MessageEvent}
import fs2.kafka._

object ConsumerApp extends IOApp {

  override def run(args: List[String]): IO[ExitCode] =
    consumerResource[IO].using(settings).use { consumer =>
      for {
        subscription <- Subscription.make[IO]
        handler <- MessageEventHandler.make[IO]
        _ <- subscription.subscribe(classOf[MessageEvent], handler)
        subscriber <- KafkaMessageSubscriber
                       .make[IO](consumer, subscription)
        _ <- subscriber.execute("fs2.topic").compile.drain
      } yield ExitCode.Success
    }

  private def keyDeserializer: Deserializer[IO, String] =
    Deserializer[IO, String]

  private def valueDeserializer: Deserializer[IO, Message] =
    Deserializer.delegate[IO, Message](JsonSerde())

  private def settings: ConsumerSettings[IO, String, Message] =
    ConsumerSettings(
      keyDeserializer = keyDeserializer,
      valueDeserializer = valueDeserializer
    ).withBootstrapServers("localhost:9092")
      .withGroupId("fs2.group")
      .withEnableAutoCommit(false)
      .withAutoOffsetReset(AutoOffsetReset.Earliest)
      .withIsolationLevel(IsolationLevel.ReadCommitted)
}
