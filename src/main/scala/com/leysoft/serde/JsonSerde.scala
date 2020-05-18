package com.leysoft.serde

import java.util

import com.leysoft.domain.Message
import org.apache.kafka.common.serialization.{Deserializer, Serde, Serializer, StringDeserializer, StringSerializer}

case class JsonSerde[A <: Message]()
    extends Serializer[A]
    with Deserializer[A]
    with Serde[A] {

  private val s = new StringSerializer

  private val d = new StringDeserializer

  override def configure(configs: util.Map[String, _], isKey: Boolean): Unit =
    super.configure(configs, isKey)

  override def close(): Unit = super.close()

  override def serialize(topic: String, data: A): Array[Byte] =
    s.serialize(topic, Json.write(data))

  override def deserialize(topic: String, data: Array[Byte]): A =
    Json.read(d.deserialize(topic, data))

  override def serializer(): Serializer[A] = this

  override def deserializer(): Deserializer[A] = this
}
