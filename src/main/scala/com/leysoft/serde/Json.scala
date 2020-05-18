package com.leysoft.serde

import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper, SerializationFeature}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.leysoft.domain.Message

import scala.util.{Success, Try}

object Json {

  private val mapper = new ObjectMapper()
  mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
  mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
  mapper.enableDefaultTyping()
  mapper.registerModule(DefaultScalaModule)

  @throws(classOf[RuntimeException])
  def write[A <: Message](data: A) =
    Try(mapper.writeValueAsString(data)) match {
      case Success(value) => value
      case _              => throw new RuntimeException("Write Error")
    }

  @throws(classOf[RuntimeException])
  def read[A <: Message](data: String) =
    Try(mapper.readValue(data, classOf[Message]).asInstanceOf[A]) match {
      case Success(value) => value
      case _              => throw new RuntimeException("Read Error")
    }
}
