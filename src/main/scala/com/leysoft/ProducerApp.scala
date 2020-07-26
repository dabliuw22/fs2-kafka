package com.leysoft

import java.util.UUID

import cats.effect.ExitCode
import com.leysoft.adapters.config._
import com.leysoft.adapters.KafkaMessagePublisher
import com.leysoft.domain.{MessageEvent, Metadata, SecondMessageEvent}
import fs2.kafka._
import fs2.Stream
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import monix.eval.{Task, TaskApp}

object ProducerApp extends TaskApp {

  private val logger = Slf4jLogger.getLogger[Task]

  private implicit val sc = scheduler

  override def run(args: List[String]): Task[ExitCode] =
    config[Task]
      .map(conf => Settings.Producer.settings[Task](conf.kafka.producer))
      .use { settings =>
        producerResource[Task]
          .using(settings)
          .use { producer =>
            shutdown()
            for {
              publisher <- KafkaMessagePublisher.make[Task](producer, settings)
              _ <- Stream("Fs2", "Cats", "Kafka")
                    .map(
                      MessageEvent(_,
                                   Metadata(topic = "fs2.topic",
                                            key = UUID.randomUUID.toString))
                    )
                    .evalMap(publisher.publish)
                    .compile
                    .drain
              _ <- publisher
                    .publish(
                      SecondMessageEvent(
                        "Monix",
                        Metadata(topic = "fs2.topic",
                                 key = UUID.randomUUID.toString)
                      )
                    )
            } yield ExitCode.Success
          }
      }

  private def shutdown(): Unit =
    sys.addShutdownHook(logger.warn("End...").runAsyncAndForget)
}
