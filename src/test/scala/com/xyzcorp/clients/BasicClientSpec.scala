package com.xyzcorp.clients

import java.util.concurrent.TimeUnit

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.scaladsl.{Sink, Source}
import akka.util.ByteString
import org.scalatest.{FunSuite, Matchers}
import spray.json.{DefaultJsonProtocol, PrettyPrinter, RootJsonFormat}

import scala.concurrent.Await
import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.util.{Failure, Success, Try}

class BasicClientSpec extends FunSuite with Matchers {
  test("Basic Client Connection with Single") {
    import akka.http.scaladsl.Http
    import akka.http.scaladsl.model._
    import akka.stream.ActorMaterializer

    import scala.concurrent.Future

    implicit val system = ActorSystem("MySystem")
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher

    val responseFuture: Future[HttpResponse] =
      Http().singleRequest(HttpRequest(uri = "http://www.nytimes.com"))

    responseFuture.foreach(resp =>
      resp.entity.dataBytes.runFold(ByteString.empty)
      ((totalByteString: ByteString, byteString: ByteString) =>
        totalByteString ++ byteString)
        .map(_.utf8String).foreach(println))

    Thread.sleep(10000)
    Await.result(system.terminate, Duration.apply(10, TimeUnit.SECONDS))
  }

  test("Basic Client Connection with HostConnectionPool") {
    import akka.http.scaladsl.Http
    import akka.http.scaladsl.model._
    import akka.stream.ActorMaterializer

    import scala.concurrent.Future

    implicit val system = ActorSystem("MySystem")
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher

    val poolClientFlow = Http().cachedHostConnectionPool[NotUsed]("akka.io")
    val responseFuture: Future[(Try[HttpResponse], NotUsed)] =
      Source.single(HttpRequest(uri = "/") -> NotUsed)
        .via(poolClientFlow)
        .runWith(Sink.head)

    responseFuture.onSuccess {
      case (Success(rs), _) =>
        println(rs.getHeaders())
      case (Failure(e), _) =>
        e.printStackTrace()
    }

    Thread.sleep(10000)
    Await.result(system.terminate, Duration.apply(10, TimeUnit.SECONDS))
  }

  test("Request Client Side API") {
    import akka.http.scaladsl.Http
    import akka.http.scaladsl.model._
    import akka.stream.ActorMaterializer

    import scala.concurrent.Future

    implicit val system = ActorSystem("MySystem")
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher

    val poolClientFlow = Http().cachedHostConnectionPool[NotUsed]("akka.io")
    val responseFuture: Future[(Try[HttpResponse], NotUsed)] =
      Source.single(HttpRequest(uri = "/") -> NotUsed)
        .via(poolClientFlow)
        .runWith(Sink.head)

    responseFuture.onSuccess {
      case (Success(rs), _: NotUsed) =>
        println(rs.getHeaders())
      case (Failure(e), _:NotUsed) =>
        e.printStackTrace()
    }

    Thread.sleep(10000)
    Await.result(system.terminate, Duration.apply(10, TimeUnit.SECONDS))
  }

  test("Marshall JSON after call") {
    import akka.http.scaladsl.Http
    import akka.http.scaladsl.model._
    import akka.stream.ActorMaterializer

    import scala.concurrent.Future

    val url = "https://query.yahooapis.com/v1/public/yql?q=select%20item.condition%20from%20weather.forecast%20where%20woeid%20%3D%202487889&format=json&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys"

    case class Root(query: Query)

    case class Query(count: Int,
                     created: String,
                     lang: String,
                     results: Results)

    case class Results(channel: Channel)

    case class Channel(item: Item)

    case class Item(condition: Condition)

    case class Condition(code: String, date: String, temp: String, text: String)

    implicit val system = ActorSystem("MySystem")
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher

    trait PrettyJsonFormatSupport extends DefaultJsonProtocol with SprayJsonSupport {
      implicit val prettyPrintedConditionFormat: RootJsonFormat[Condition] = jsonFormat4(Condition)
      implicit val prettyPrintedItemFormat: RootJsonFormat[Item] = jsonFormat1(Item)
      implicit val prettyPrintedChannelFormat: RootJsonFormat[Channel] = jsonFormat1(Channel)
      implicit val prettyPrintedResultsFormat: RootJsonFormat[Results] = jsonFormat1(Results)
      implicit val prettyPrintedQueryFormat: RootJsonFormat[Query] = jsonFormat4(Query)
      implicit val prettyPrintedRootFormat: RootJsonFormat[Root] = jsonFormat1(Root)
      implicit val printer = PrettyPrinter
    }

    val finiteDuration: FiniteDuration = Duration.apply(10, TimeUnit.SECONDS)

    new PrettyJsonFormatSupport {
      val responseFuture: Future[HttpResponse] =
        Http().singleRequest(HttpRequest(uri = url))

      private val rootFuture: Future[Root] = responseFuture
        .flatMap(response => Unmarshal(response.entity).to[Root])

      rootFuture.onSuccess {
        case r: Root => println(r)
      }

      rootFuture.onFailure {
        case t: Throwable => t.printStackTrace()
      }

      Await.result(rootFuture, finiteDuration)
    }

    materializer.shutdown()
    Http().shutdownAllConnectionPools().
      onComplete(_ => Await.result(system.terminate, finiteDuration))
  }
}
