package com.leysoft.domain

import io.chrisdavenport.log4cats.Logger

trait MessageHandler[F[_]] {

  def execute[A <: Message](message: A): fs2.Stream[F, Unit]

  def unit[A <: Message](logger: Logger[F], message: A): fs2.Stream[F, Unit] =
    fs2.Stream
      .eval(logger.error(s"This handler cannot handle the message: $message"))
      .covary[F]
}
