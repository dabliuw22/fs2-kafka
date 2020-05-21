package com.leysoft.domain

trait MessageHandler[F[_]] {

  def execute[A](message: A): fs2.Stream[F, Unit]
}
