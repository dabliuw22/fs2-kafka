package com.leysoft

import com.fasterxml.jackson.annotation.JsonTypeInfo

package object domain {

  @JsonTypeInfo(
    use = JsonTypeInfo.Id.CLASS,
    include = JsonTypeInfo.As.PROPERTY,
    property = "@class"
  )
  abstract class Message

  case class MessageEvent(data: String) extends Message
}
