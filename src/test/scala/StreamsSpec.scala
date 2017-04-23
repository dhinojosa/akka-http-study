import java.util.concurrent.TimeUnit

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Source}
import org.scalatest.{FunSuite, Matchers}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class StreamsSpec extends FunSuite with Matchers {
  test(
    """can also have a Flow, which
      |is just an interconnecting piece that can be reused""".stripMargin) {

    implicit val system = ActorSystem("MyActorSystem")
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher

    val flow: Flow[Int, Int, NotUsed] =
      Flow.fromFunction((x: Int) => x + 1)

    val future = Future {
      Thread.sleep(1000)
      1000
    }

    Source.fromFuture(future).via(flow).runForeach(println)

    Thread.sleep(2000)

    Await.result(system.terminate(), Duration(10, TimeUnit.SECONDS))
  }
}
