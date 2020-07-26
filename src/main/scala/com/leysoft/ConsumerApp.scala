package com.leysoft

import cats.effect.{ContextShift, Effect, ExitCode, IO, IOApp, Resource}
import cats.syntax.flatMap._
import cats.syntax.functor._
import com.leysoft.adapters.config._
import com.leysoft.adapters.{KafkaMessagePublisher, KafkaMessageSubscriber, Subscription}
import com.leysoft.application.{MessageEventHandler, NewMessageHandler, SecondMessageEventHandler}
import com.leysoft.domain.{Message, MessageEvent, MessagePublisher, SecondMessageEvent}
import fs2.kafka._
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger

object ConsumerApp extends IOApp {

  private val logger = Slf4jLogger.getLogger[IO]

  override def run(args: List[String]): IO[ExitCode] =
    loadConfig[IO]
      .use { settings =>
        consumerResource[IO].using(settings._1).use { consumer =>
          producerResource[IO].using(settings._2).use { producer =>
            shutdown()
            for {
              publisher <- KafkaMessagePublisher.make[IO](producer, settings._2)
              subscription <- subscription[IO](publisher)
              subscriber <- KafkaMessageSubscriber
                             .make[IO](consumer, subscription)
              _ <- subscriber.execute("fs2.topic").compile.drain
            } yield ExitCode.Success
          }
        }
      }

  private def loadConfig[F[_]: Effect: ContextShift]
    : Resource[F,
               (ConsumerSettings[F, String, Message],
                ProducerSettings[F, String, Message])] =
    config[F]
      .map(
        conf =>
          (Settings.Consumer.settings[F](conf.kafka.consumer),
           Settings.Producer.settings[F](conf.kafka.producer))
      )

  private def subscription[F[_]: Effect](
    publisher: MessagePublisher[F]
  ): F[Subscription[F]] =
    for {
      subscription <- Subscription.make[F]
      firstHandler <- MessageEventHandler.make[F](publisher)
      newHandler <- NewMessageHandler.make[F]
      secondHandler <- SecondMessageEventHandler.make[F]
      _ <- subscription.subscribe(classOf[MessageEvent], firstHandler)
      _ <- subscription.subscribe(classOf[MessageEvent], newHandler)
      _ <- subscription.subscribe(classOf[SecondMessageEvent], secondHandler)
    } yield subscription

  private def shutdown(): Unit =
    sys.addShutdownHook(logger.warn("End...").unsafeRunSync())
}
