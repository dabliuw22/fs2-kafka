package com.leysoft.application

import cats.effect.Effect
import com.leysoft.domain.{Message, MessageHandler, SecondMessageEvent}
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger

final class SecondMessageEventHandler[F[_]: Effect] private ()
    extends MessageHandler[F] {

  private val logger =
    Slf4jLogger.getLoggerFromClass[F](classOf[SecondMessageEventHandler[F]])

  override def execute[A <: Message](message: A): fs2.Stream[F, Unit] =
    message match {
      case m: SecondMessageEvent =>
        fs2.Stream
          .eval(logger.info(s"Second Execute: $m"))
          .covary[F]
          .evalMap(_ => logger.info(s"Second Finalize: $m"))
      case _ => unit(logger, message)
    }
}

object SecondMessageEventHandler {

  def make[F[_]: Effect]: F[MessageHandler[F]] =
    Effect[F].delay(new SecondMessageEventHandler[F])
}
