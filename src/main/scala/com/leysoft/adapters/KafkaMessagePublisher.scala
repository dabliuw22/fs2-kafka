package com.leysoft.adapters

import cats.effect.{ConcurrentEffect, ContextShift}
import com.leysoft.domain.{Message, MessagePublisher, Metadata}
import fs2.kafka._
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger

final class KafkaMessagePublisher[F[_]: ConcurrentEffect: ContextShift] private (
  val producer: KafkaProducer[F, String, Message],
  val settings: ProducerSettings[F, String, Message]
) extends MessagePublisher[F] {

  private val logger =
    Slf4jLogger.getLoggerFromClass[F](classOf[KafkaMessagePublisher[F]])

  override def publish[A <: Message](
    message: A
  ): fs2.Stream[F, Metadata] =
    fs2.Stream
      .emit(message)
      .map { message =>
        val record =
          ProducerRecord(message.metadata.topic, message.metadata.key, message)
        ProducerRecords.one(record)
      }
      .covary[F]
      .through(produce(settings, producer))
      .as(message.metadata)
      .onFinalize(logger.info(s"Publish: $message"))
}

object KafkaMessagePublisher {

  def make[F[_]: ConcurrentEffect: ContextShift](
    producer: KafkaProducer[F, String, Message],
    settings: ProducerSettings[F, String, Message]
  ): F[MessagePublisher[F]] =
    ConcurrentEffect[F].delay(new KafkaMessagePublisher[F](producer, settings))
}
