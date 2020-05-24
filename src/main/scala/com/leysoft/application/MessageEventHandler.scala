package com.leysoft.application

import cats.effect.Effect
import com.leysoft.domain.{Message, MessageEvent, MessageHandler, SecondMessageEvent}
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger

final class MessageEventHandler[F[_]: Effect] private ()
    extends MessageHandler[F] {

  private val logger =
    Slf4jLogger.getLoggerFromClass[F](classOf[MessageEventHandler[F]])

  override def execute[A <: Message](message: A): fs2.Stream[F, Unit] =
    message match {
      case m: MessageEvent =>
        fs2.Stream
          .eval(logger.info(s"Execute: $m")) >> fs2.Stream.empty.covary[F]
      case _ => fs2.Stream.empty.covary[F]
    }
}

object MessageEventHandler {

  def make[F[_]: Effect]: F[MessageHandler[F]] =
    Effect[F].delay(new MessageEventHandler[F])
}
