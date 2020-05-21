package com.leysoft.adapters

import cats.effect.Effect
import com.leysoft.domain.{Message, MessageHandler}

final class Subscription[F[_]: Effect] private () {

  val subscribers =
    collection.mutable.Map[Class[_], List[MessageHandler[F]]]()

  def subscribe[A <: Message](clazz: Class[A],
                              handler: MessageHandler[F]): F[Unit] = {
    Effect[F].delay {
      subscribers.get(clazz) match {
        case Some(list) =>
          subscribers.put(clazz, list.appended(handler))
        case _ =>
          subscribers.put(clazz, List(handler))
      }
    }
  }

  def run[A <: Message](message: A): fs2.Stream[F, Unit] =
    subscribers.get(message.getClass) match {
      case Some(list) =>
        fs2.Stream.emits(list).covary[F].flatMap(_.execute(message))
      case _ =>
        fs2.Stream.raiseError(
          new RuntimeException(
            s"There are no handlers for: ${message.getClass}"
          )
        )
    }
}

object Subscription {

  def make[F[_]: Effect]: F[Subscription[F]] =
    Effect[F].delay(new Subscription[F])
}
