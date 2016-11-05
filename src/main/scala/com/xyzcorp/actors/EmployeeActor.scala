package com.xyzcorp.actors

import akka.actor.Actor
import akka.event.Logging
import com.xyzcorp.entities.Employee

class EmployeeActor extends Actor {
  val log = Logging(context.system, this)

  val contents = scala.collection.mutable.ArrayBuffer(Employee("Ozzy", "Osbourne"),
    Employee("Frank", "Sinatra"),
    Employee("Neil", "Diamond"),
    Employee("Joni", "Mitchell"))

  override def receive = {
    case x: Int =>
      log.debug("Recieved message, and looking up {} in a datastore", x)
      if (x > contents.size - 1) sender ! None
      sender ! Some(contents.apply(x))
    case x: Employee =>
      contents += x
    case (id:Int, e:Employee) =>
      contents(id) = e
    case u =>
      log.debug("Didn't understand message, sending message to dead letters")
      unhandled(u)
  }
}
