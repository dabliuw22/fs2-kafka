package com.leysoft

import cats.effect.ExitCode
import com.leysoft.adapters.config._
import com.leysoft.adapters.KafkaMessagePublisher
import com.leysoft.domain.{MessageEvent, Metadata}
import fs2.kafka._
import fs2.Stream
import monix.eval.{Task, TaskApp}

object ProducerApp extends TaskApp {

  override def run(args: List[String]): Task[ExitCode] =
    config[Task]
      .map(conf => Settings.Producer.settings[Task](conf.kafka.producer))
      .use { settings =>
        producerResource[Task]
          .using(settings)
          .use { producer =>
            for {

              publisher <- KafkaMessagePublisher.make[Task](producer, settings)
              _ <- Stream("Fs2", "Cats", "Kafka")
                    .map(
                      MessageEvent(_,
                                   Metadata(topic = "fs2.topic",
                                            key = "fs2.key"))
                    )
                    .flatMap(publisher.publish)
                    .compile
                    .drain
            } yield ExitCode.Success
          }
      }
}
