package com.leysoft

import cats.effect.{ExitCode, IO, IOApp}
import com.leysoft.serde.JsonSerde
import com.leysoft.domain.Message
import fs2.kafka._
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger

import scala.concurrent.duration._

object ConsumerApp extends IOApp {
  import cats.syntax.functor._ // for as()

  val logger = Slf4jLogger.getLoggerFromClass[IO](ConsumerApp.getClass)

  override def run(args: List[String]): IO[ExitCode] =
    consumerStream[IO]
      .using(settings)
      .evalTap(_.subscribeTo("fs2.topic"))
      .flatMap(_.stream)
      .mapAsync(20) { message =>
        process(message.record).as(message.offset)
      }
      .through(commitBatchWithin(500, 10 seconds))
      .compile
      .drain
      .as(ExitCode.Success)

  def process(record: ConsumerRecord[String, Message]): IO[Unit] =
    logger.info(s"Message: ${record.value}")

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
