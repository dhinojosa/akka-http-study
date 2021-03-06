package com.xyzcorp.servers

import java.util.concurrent.TimeUnit

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model.{FormData, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.unmarshalling.FromRequestUnmarshaller
import akka.pattern._
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.xyzcorp.actors.EmployeeActor
import com.xyzcorp.entities.Employee

import scala.io.StdIn

object BasicGetPostPutServer {
  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem("my-system")
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher
    implicit val timeout = Timeout(5, TimeUnit.SECONDS)

    implicit val requestFormDataToEmployeeMarshaller: FromRequestUnmarshaller[Employee] = implicitly[FromRequestUnmarshaller[FormData]]
      .map(fd => {
        val fields: Query = fd.fields
        fields.get("firstName").flatMap(fn => fields.get("lastName").map(ln => Employee(fn, ln)))
      }.get)

    val employeeActor = system.actorOf(Props[EmployeeActor], "EmployeeFinder")
    val route =
      path("employee" / IntNumber) { number =>
        get {
          val future = employeeActor ? number
          onSuccess(future) {
            case Some(Employee(fn, ln)) => complete(s"$fn $ln")
            case None => complete(StatusCodes.NotFound)
          }
        } ~ put {
          entity(as[Employee]) { data =>
            employeeActor ! (number, data)
            complete("Employee Updated")
          }
        }
      } ~
        path("employee") {
          post {
            entity(as[Employee]) { data =>
              employeeActor ! data
              complete("Employee Received")
            }
          }
        }

    val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)

    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }
}
