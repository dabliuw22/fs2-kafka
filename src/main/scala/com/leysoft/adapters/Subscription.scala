package com.leysoft.adapters

import cats.effect.Effect
import com.leysoft.domain.{Message, MessageHandler}
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger

final class Subscription[F[_]: Effect] private () {

  private val logger =
    Slf4jLogger.getLoggerFromClass[F](classOf[Subscription[F]])

  val subscribers =
    collection.mutable.Map[Class[_], List[MessageHandler[F]]]()

  def subscribe[A <: Message](clazz: Class[A],
                              handler: MessageHandler[F]): F[Unit] = {
    Effect[F].delay {
      subscribers.get(clazz) match {
        case Some(handlers) =>
          subscribers.put(clazz, handlers.appended(handler))
        case _ =>
          subscribers.put(clazz, List(handler))
      }
    }
  }

  def run[A <: Message](message: A): fs2.Stream[F, Unit] =
    subscribers.get(message.getClass) match {
      case Some(handlers) =>
        fs2.Stream.emits(handlers).covary[F].flatMap(_.execute(message))
      case _ =>
        fs2.Stream.eval(
          logger.error(s"Error: There are no handlers for: ${message.getClass}")
        ) >> fs2.Stream.empty.covary[F]
    }
}

object Subscription {

  def make[F[_]: Effect]: F[Subscription[F]] =
    Effect[F].delay(new Subscription[F])
}
