package com.leysoft.domain

trait MessageHandler[F[_]] {

  def execute[A <: Message](message: A): fs2.Stream[F, Message]
}
