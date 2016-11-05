package com.xyzcorp.servers

import java.util.concurrent.TimeUnit

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives.Credentials
import akka.pattern._
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.xyzcorp.actors.EmployeeActor
import com.xyzcorp.entities.Employee
import spray.json.DefaultJsonProtocol._

import scala.io.StdIn

object SecureJSONGetPostPutServer {
  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem("my-system")
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher
    implicit val timeout = Timeout(5, TimeUnit.SECONDS)

    implicit val employeeFormat = jsonFormat2(Employee)

    def myUserPassAuthenticator(credentials: Credentials): Option[String] =
      credentials match {
        case p@Credentials.Provided(id) if p.verify("AkkaTime") => Some(id)
        case _ => None
      }

    val employeeActor = system.actorOf(Props[EmployeeActor], "EmployeeFinder")
    val route =
      authenticateBasic(realm = "EvilRealm", myUserPassAuthenticator) { user =>
        path("employee" / IntNumber) { number =>
          get {
            val future = employeeActor ? number

            onSuccess(future) {
              case Some(e: Employee) => complete(e)
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
              entity(as[Employee]) { e =>
                employeeActor ! e
                complete("Employee Received")
              }
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
