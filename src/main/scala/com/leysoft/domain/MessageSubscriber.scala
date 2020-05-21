package com.leysoft.domain

trait MessageSubscriber[F[_]] {

  def execute(topics: String*): fs2.Stream[F, Unit]
}
