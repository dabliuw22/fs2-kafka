package com.leysoft.adapters

import cats.effect.Effect
import com.leysoft.domain.MessageHandler

final class MessageEventHandler[F[_]: Effect] private ()
    extends MessageHandler[F] {

  override def execute[A](message: A): fs2.Stream[F, Unit] =
    fs2.Stream.emit(println(s"Execute: $message")).covary[F]
}

object MessageEventHandler {

  def make[F[_]: Effect]: F[MessageHandler[F]] =
    Effect[F].delay(new MessageEventHandler[F])
}
