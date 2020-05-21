package com.leysoft.adapters

import cats.effect.{ConcurrentEffect, Timer}
import com.leysoft.domain.{Message, MessageSubscriber}
import fs2.kafka._

import scala.concurrent.duration._

final class KafkaMessageSubscriber[F[_]: ConcurrentEffect: Timer] private (
  val consumer: KafkaConsumer[F, String, Message],
  val subscription: Subscription[F]
) extends MessageSubscriber[F] {

  override def execute(topics: String*): fs2.Stream[F, Unit] =
    fs2.Stream
      .emit(consumer)
      .covary[F]
      .evalTap(_.subscribeTo(topics.head, topics.tail: _*))
      .flatMap(_.stream)
      .flatMap { message =>
        subscription.run(message.record.value).as(message.offset)
      }
      .through(commitBatchWithin(500, 10 seconds))
}

object KafkaMessageSubscriber {

  def make[F[_]: ConcurrentEffect: Timer](
    consumer: KafkaConsumer[F, String, Message],
    subscription: Subscription[F]
  ): F[MessageSubscriber[F]] =
    ConcurrentEffect[F].delay(
      new KafkaMessageSubscriber[F](consumer, subscription)
    )
}