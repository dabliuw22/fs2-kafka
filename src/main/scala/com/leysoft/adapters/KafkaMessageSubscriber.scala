package com.leysoft.adapters

import cats.effect.{ConcurrentEffect, Timer}
import com.leysoft.domain.{Message, MessageSubscriber}
import fs2.kafka._
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger

import scala.concurrent.duration._

final class KafkaMessageSubscriber[F[_]: ConcurrentEffect: Timer] private (
  val consumer: KafkaConsumer[F, String, Message],
  val subscription: Subscription[F]
) extends MessageSubscriber[F] {

  private val logger =
    Slf4jLogger.getLoggerFromClass[F](classOf[KafkaMessageSubscriber[F]])

  override def execute(topics: String*): fs2.Stream[F, Unit] =
    fs2.Stream
      .emit(consumer)
      .covary[F]
      .evalTap(_.subscribeTo(topics.head, topics.tail: _*))
      .flatMap(_.stream)
      .flatMap(message => run(message))
      .map(message => message.offset)
      .through(commitBatchWithin(100, 500 milliseconds))

  private def run(message: CommittableConsumerRecord[F, String, Message]) =
    subscription
      .run(message.record.value)
      .handleErrorWith(
        _ =>
          fs2.Stream.eval(
            logger
              .error(s"Error consuming: ${message.record.value.getClass}")
          ) >> fs2.Stream.empty.covary[F]
      )
      .hold(())
      .as(message)
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
