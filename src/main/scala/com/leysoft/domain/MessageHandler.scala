package com.leysoft.domain

trait MessageHandler[F[_]] {

  def execute[A <: Message](message: A): fs2.Stream[F, Unit]

  def unit: fs2.Stream[F, Unit] = fs2.Stream.emit(()).covary[F]
}
