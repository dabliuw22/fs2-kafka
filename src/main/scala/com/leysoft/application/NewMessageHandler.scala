package com.leysoft.application

import cats.effect.Effect
import com.leysoft.domain.{Message, MessageEvent, MessageHandler}
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger

final class NewMessageHandler[F[_]: Effect] private ()
    extends MessageHandler[F] {

  private val logger =
    Slf4jLogger.getLoggerFromClass[F](classOf[NewMessageHandler[F]])

  override def execute[A <: Message](message: A): fs2.Stream[F, Unit] =
    message match {
      case m: MessageEvent =>
        fs2.Stream
          .eval(logger.info(s"New Execute: $m")) >> fs2.Stream
          .raiseError(new RuntimeException)
          .evalMap(_ => logger.info(s"New Finalize: $m"))
      case _ => unit(logger, message)
    }
}

object NewMessageHandler {

  def make[F[_]: Effect]: F[MessageHandler[F]] =
    Effect[F].delay(new NewMessageHandler[F])
}
