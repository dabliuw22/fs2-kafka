package com.leysoft.adapters

import cats.effect.Effect
import cats.effect.concurrent.Ref
import cats.syntax.flatMap._
import cats.syntax.functor._
import com.leysoft.adapters.Subscription.Subscriptions
import com.leysoft.domain.{Message, MessageHandler}
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger

final class Subscription[F[_]: Effect] private (
  subscriptions: Subscriptions[F]
) {

  private val logger =
    Slf4jLogger.getLoggerFromClass[F](classOf[Subscription[F]])

  def subscribe[A <: Message](clazz: Class[A],
                              handler: MessageHandler[F]): F[Unit] = {
    subscriptions.get.flatMap(
      _.get(clazz) match {
        case Some(handlers) =>
          subscriptions.update(_.updated(clazz, handlers.appended(handler)))
        case _ => subscriptions.update(_.updated(clazz, List(handler)))
      }
    )
  }

  def run[A <: Message](message: A): fs2.Stream[F, Unit] =
    fs2.Stream
      .eval { subscriptions.get.map(_.get(message.getClass)) }
      .flatMap {
        case Some(handlers) =>
          fs2.Stream
            .emits(handlers)
            .covary[F]
            .flatMap { handler =>
              handler
                .execute(message)
                .handleErrorWith(
                  _ =>
                    error(
                      s"Handler: ${handler.getClass}, Error consuming: ${message.getClass}"
                  )
                )
            }
        case _ =>
          error(s"Error: There are no handlers for: ${message.getClass}")
      }

  private lazy val error: String => fs2.Stream[F, Unit] = message =>
    fs2.Stream.eval(logger.error(message))
}

object Subscription {

  type Subscriptions[F[_]] = Ref[F, Map[Class[_], List[MessageHandler[F]]]]

  def make[F[_]: Effect]: F[Subscription[F]] =
    Ref
      .of(Map[Class[_], List[MessageHandler[F]]]().empty)
      .map(subscriptions => new Subscription[F](subscriptions))
}
