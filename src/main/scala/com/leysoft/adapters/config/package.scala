package com.leysoft.adapters

import cats.effect.{ContextShift, Effect, Resource}
import cats.implicits._
import ciris.ConfigValue
import ciris._
import ciris.refined._
import eu.timepit.refined.W
import eu.timepit.refined.api.Refined
import eu.timepit.refined.auto._
import eu.timepit.refined.collection.MaxSize
import eu.timepit.refined.types.numeric.PosInt
import eu.timepit.refined.types.string.NonEmptyString

import scala.concurrent.duration._

package object config {

  type KafkaServer = NonEmptyString

  type KafkaClientId = NonEmptyString

  type KafkaLinger = FiniteDuration

  type KafkaBatchSize = PosInt

  type KafkaIdempotence = Boolean

  type KafkaMaxInFlightRequestsPerConnection = PosInt

  type KafkaRetries = PosInt

  type KafkaCompression = String Refined MaxSize[W.`6`.T]

  type KafkaGroupId = NonEmptyString

  type KafkaAutoCommit = Boolean

  final case class KafkaProducerConfiguration(
    server: KafkaServer,
    clientId: KafkaClientId,
    linger: KafkaLinger,
    batchSize: KafkaBatchSize,
    idempotence: KafkaIdempotence,
    requestsPerConnection: KafkaMaxInFlightRequestsPerConnection,
    retries: KafkaRetries,
    compression: KafkaCompression
  )

  final case class KafkaConsumerConfiguration(
    server: KafkaServer,
    groupId: KafkaGroupId,
    autoCommit: KafkaAutoCommit
  )

  final case class KafkaConfiguration(
    producer: KafkaProducerConfiguration,
    consumer: KafkaConsumerConfiguration
  )

  final case class Configuration(kafka: KafkaConfiguration)

  val producerConfig: ConfigValue[KafkaProducerConfiguration] =
    (
      env("KAFKA_SERVER").as[KafkaServer].default("localhost:9092"),
      env("KAFKA_CLIENT_ID").as[KafkaClientId].default("fs2.client"),
      env("KAFKA_LINGER_MS")
        .as[PosInt]
        .default(10)
        .map(millis => FiniteDuration.apply(millis.value, MILLISECONDS)),
      env("KAFKA_BATCH_SIZE").as[KafkaBatchSize].default(500),
      env("KAFKA_IDEMPOTENCE").as[KafkaIdempotence].default(true),
      env("KAFKA_REQUEST_PER_CONNECTION")
        .as[KafkaMaxInFlightRequestsPerConnection]
        .default(3),
      env("KAFKA_RETRIES").as[KafkaRetries].default(3),
      env("KAFKA_COMPRESSION").as[KafkaCompression].default("snappy")
    ).parMapN {
      (server,
       clientId,
       linger,
       batchSize,
       idempotence,
       request,
       retries,
       compression) =>
        KafkaProducerConfiguration(
          server,
          clientId,
          linger,
          batchSize,
          idempotence,
          request,
          retries,
          compression
        )
    }

  private def consumerConfig: ConfigValue[KafkaConsumerConfiguration] =
    (
      env("KAFKA_SERVER").as[KafkaServer].default("localhost:9092"),
      env("KAFKA_GROUP_ID").as[KafkaGroupId].default("fs2.group"),
      env("KAFKA_AUTO_COMMIT").as[KafkaAutoCommit].default(false)
    ).parMapN { (server, groupId, autoCommit) =>
      KafkaConsumerConfiguration(server, groupId, autoCommit)
    }

  private def kafkaConfig: ConfigValue[KafkaConfiguration] =
    producerConfig.flatMap(
      producer =>
        consumerConfig.map(consumer => KafkaConfiguration(producer, consumer))
    )

  private def configuration: ConfigValue[Configuration] =
    kafkaConfig.map(kafka => Configuration(kafka))

  def config[F[_]: Effect: ContextShift]: Resource[F, Configuration] =
    Resource.liftF(configuration.load[F])
}
