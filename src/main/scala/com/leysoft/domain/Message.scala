package com.leysoft.domain

import com.fasterxml.jackson.annotation.JsonSubTypes.Type
import com.fasterxml.jackson.annotation.{JsonSubTypes, JsonTypeInfo}
import com.fasterxml.jackson.databind.annotation.{JsonDeserialize, JsonSerialize}

@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  include = JsonTypeInfo.As.PROPERTY,
  property = "type"
)
@JsonSubTypes(
  value = Array(
    new Type(value = classOf[MessageEvent], name = "MessageEvent")
  )
)
abstract class Message

@JsonSerialize(as = classOf[MessageEvent])
@JsonDeserialize(as = classOf[MessageEvent])
case class MessageEvent(data: String) extends Message
