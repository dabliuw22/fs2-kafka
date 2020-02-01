package com.leysoft.serde

import java.util

import com.leysoft.domain.Message
import org.apache.kafka.common.serialization.{Deserializer, Serde, Serializer, StringDeserializer, StringSerializer}

case class JsonSerde() extends Serializer[Message] with Deserializer[Message] with Serde[Message] {

  private val s = new StringSerializer

  private val d = new StringDeserializer

  override def configure(configs: util.Map[String, _], isKey: Boolean): Unit = super.configure(configs, isKey)

  override def close(): Unit = super.close()

  override def serialize(topic: String, data: Message): Array[Byte] = s.serialize(topic, Json.write(data))

  override def deserialize(topic: String, data: Array[Byte]): Message = Json.read(d.deserialize(topic, data))

  override def serializer(): Serializer[Message] = this

  override def deserializer(): Deserializer[Message] = this
}
