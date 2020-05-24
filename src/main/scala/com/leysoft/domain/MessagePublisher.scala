package com.leysoft.domain

trait MessagePublisher[F[_]] {

  def publish[A <: Message](message: A): F[Metadata]
}
