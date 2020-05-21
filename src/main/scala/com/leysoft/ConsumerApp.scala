package com.leysoft

import cats.effect.{ExitCode, IO, IOApp}
import com.leysoft.adapters.config.{KafkaConsumerConfiguration, config}
import com.leysoft.adapters.{KafkaMessageSubscriber, MessageEventHandler, Subscription}
import com.leysoft.adapters.serde.JsonSerde
import com.leysoft.domain.{Message, MessageEvent}
import fs2.kafka._

object ConsumerApp extends IOApp {

  override def run(args: List[String]): IO[ExitCode] =
    config[IO]
      .map(conf => settings(conf.kafka.consumer))
      .use { settings =>
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
      }

  private def keyDeserializer: Deserializer[IO, String] =
    Deserializer[IO, String]

  private def valueDeserializer: Deserializer[IO, Message] =
    Deserializer.delegate[IO, Message](JsonSerde())

  private def settings(
    config: KafkaConsumerConfiguration
  ): ConsumerSettings[IO, String, Message] = {
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
