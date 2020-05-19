package com.leysoft.serde

import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper, SerializationFeature}
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.leysoft.domain.Message

import scala.util.{Success, Try}

object Json {

  private val mapper = JsonMapper.builder
    .addModule(DefaultScalaModule)
    .addModule(new Jdk8Module)
    .addModule(new JavaTimeModule)
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
    .build

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
