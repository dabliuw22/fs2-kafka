package com.leysoft.adapters

import cats.effect.{ConcurrentEffect, ContextShift}
import com.leysoft.domain
import com.leysoft.domain.{Message, MessagePublisher}
import fs2.kafka._

final class KafkaMessagePublisher[F[_]: ConcurrentEffect: ContextShift] private (
  val producer: KafkaProducer[F, String, Message],
  val settings: ProducerSettings[F, String, Message]
) extends MessagePublisher[F] {

  override def publish[A <: domain.Message](
    message: A
  ): fs2.Stream[F, domain.Metadata] =
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
}

object KafkaMessagePublisher {

  def make[F[_]: ConcurrentEffect: ContextShift](
    producer: KafkaProducer[F, String, Message],
    settings: ProducerSettings[F, String, Message]
  ): F[MessagePublisher[F]] =
    ConcurrentEffect[F].delay(new KafkaMessagePublisher[F](producer, settings))
}
