package com.leysoft

import cats.effect.{ExitCode, IO, IOApp}
import com.leysoft.adapters.config._
import com.leysoft.adapters.{KafkaMessageSubscriber, Subscription}
import com.leysoft.application.{MessageEventHandler, SecondMessageEventHandler}
import com.leysoft.domain.MessageEvent
import fs2.kafka._

object ConsumerApp extends IOApp {

  override def run(args: List[String]): IO[ExitCode] =
    config[IO]
      .map(conf => Settings.Consumer.settings[IO](conf.kafka.consumer))
      .use { settings =>
        consumerResource[IO].using(settings).use { consumer =>
          for {
            subscription <- Subscription.make[IO]
            firstHandler <- MessageEventHandler.make[IO]
            secondHandler <- SecondMessageEventHandler.make[IO]
            _ <- subscription.subscribe(classOf[MessageEvent], firstHandler)
            _ <- subscription.subscribe(classOf[MessageEvent], secondHandler)
            subscriber <- KafkaMessageSubscriber
                           .make[IO](consumer, subscription)
            _ <- subscriber.execute("fs2.topic").compile.drain
          } yield ExitCode.Success
        }
      }
}
