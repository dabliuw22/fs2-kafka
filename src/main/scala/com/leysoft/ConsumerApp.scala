package com.leysoft

import cats.effect.{ExitCode, IO, IOApp}
import com.leysoft.adapters.config._
import com.leysoft.adapters.{KafkaMessagePublisher, KafkaMessageSubscriber, Subscription}
import com.leysoft.application.{MessageEventHandler, SecondMessageEventHandler}
import com.leysoft.domain.{MessageEvent, SecondMessageEvent}
import fs2.kafka._

object ConsumerApp extends IOApp {

  override def run(args: List[String]): IO[ExitCode] =
    config[IO]
      .map(
        conf =>
          (Settings.Consumer.settings[IO](conf.kafka.consumer),
           Settings.Producer.settings[IO](conf.kafka.producer))
      )
      .use { settings =>
        consumerResource[IO].using(settings._1).use { consumer =>
          producerResource[IO].using(settings._2).use { producer =>
            for {
              publisher <- KafkaMessagePublisher.make[IO](producer, settings._2)
              subscription <- Subscription.make[IO]
              firstHandler <- MessageEventHandler.make[IO](publisher)
              secondHandler <- SecondMessageEventHandler.make[IO]
              _ <- subscription.subscribe(classOf[MessageEvent], firstHandler)
              _ <- subscription.subscribe(classOf[SecondMessageEvent],
                                          secondHandler)
              subscriber <- KafkaMessageSubscriber
                             .make[IO](consumer, subscription)
              _ <- subscriber.execute("fs2.topic").compile.drain
            } yield ExitCode.Success
          }
        }
      }
}
