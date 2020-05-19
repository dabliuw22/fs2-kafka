package com.leysoft

import java.time.ZonedDateTime

import com.fasterxml.jackson.annotation.JsonTypeInfo

package object domain {

  case class Metadata(
    topic: String,
    createAt: ZonedDateTime = ZonedDateTime.now
  )

  @JsonTypeInfo(
    use = JsonTypeInfo.Id.CLASS,
    include = JsonTypeInfo.As.PROPERTY,
    property = "@class"
  )
  abstract class Message {

    def metadata: Metadata
  }

  case class MessageEvent(
    data: String,
    override val metadata: Metadata
  ) extends Message
}
