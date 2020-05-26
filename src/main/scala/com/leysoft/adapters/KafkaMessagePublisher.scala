package com.leysoft.adapters

import cats.effect.{ConcurrentEffect, ContextShift}
import cats.syntax.functor._
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
  ): F[Metadata] =
    fs2.Stream
      .eval(ConcurrentEffect[F].delay(message))
      .map { m =>
        val record =
          ProducerRecord(m.metadata.topic, m.metadata.key, m)
        ProducerRecords.one(record)
      }
      .through(produce(settings, producer))
      .as(message.metadata)
      .compile
      .toList
      .map {
        case head :: tail => head
        case Nil          => throw new RuntimeException(s"Message not published")
      }
}

object KafkaMessagePublisher {

  def make[F[_]: ConcurrentEffect: ContextShift](
    producer: KafkaProducer[F, String, Message],
    settings: ProducerSettings[F, String, Message]
  ): F[MessagePublisher[F]] =
    ConcurrentEffect[F].delay(new KafkaMessagePublisher[F](producer, settings))
}
