package com.leysoft.application

import cats.effect.Effect
import com.leysoft.domain.{Message, MessageEvent, MessageHandler}
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger

final class SecondMessageEventHandler[F[_]: Effect] private ()
    extends MessageHandler[F] {

  private val logger =
    Slf4jLogger.getLoggerFromClass[F](classOf[SecondMessageEventHandler[F]])

  override def execute[A <: Message](message: A): fs2.Stream[F, Unit] =
    message match {
      case m: MessageEvent =>
        fs2.Stream
          .eval(logger.info(s"Second Execute: $m")) >> fs2.Stream.empty
          .covary[F]
      case _ => fs2.Stream.empty.covary[F]
    }
}

object SecondMessageEventHandler {

  def make[F[_]: Effect]: F[MessageHandler[F]] =
    Effect[F].delay(new SecondMessageEventHandler[F])
}
