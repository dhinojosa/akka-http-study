package com.xyzcorp.actors

import akka.actor.Actor
import akka.event.Logging

class SimpleActor extends Actor {
  val log = Logging(context.system, this)

  override def receive: Receive = {
    case x:String => log.info("Received some information: {}", x)
    case _ => log.info("Received something non-sensical")
  }
}
