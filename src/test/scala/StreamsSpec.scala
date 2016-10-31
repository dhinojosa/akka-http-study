import java.nio.file.Paths
import java.util.concurrent.TimeUnit

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, IOResult}
import akka.stream.scaladsl.{FileIO, Flow, Keep, Sink, Source}
import akka.util.ByteString
import org.scalatest.{FunSuite, Matchers}

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

class StreamsSpec extends FunSuite with Matchers {

  test("Creating a stream and processing it") {
    //ActorSystem
    implicit val actorSystem = ActorSystem.create("ActorSystem")

    //Materializer
    implicit val materializer = ActorMaterializer.apply()
    //The second parameterized type is secondary information
    val source: Source[Int, NotUsed] = Source(1 to 100)
    source.runForeach(println)
  }


  test("run with a flow and sink") {

    def sink(fileName: String): Sink[String, Future[IOResult]] = {
      Flow[String].map(x => ByteString(x + "\n")).toMat(FileIO.toPath(Paths.get(fileName)))(Keep.right)
    }
    //ActorSystem
    implicit val actorSystem = ActorSystem.create("ActorSystem")

    //Materializer
    implicit val materializer = ActorMaterializer.apply()

    //The second parameterized type is secondary information
    val source: Source[Int, NotUsed] = Source(1 to 100)

    val reduce: Future[Int] = source.map(_ + 1).filter(_ % 2 == 0).runReduce(_*_)

    reduce.foreach(println)(actorSystem.dispatcher)
  }


  test("runWith which creates a Sink") {

    def sink(fileName: String): Sink[String, Future[IOResult]] = {
      Flow[String].map(x => ByteString(x + "\n")).toMat(FileIO.toPath(Paths.get(fileName)))(Keep.right)
    }
    //ActorSystem
    implicit val actorSystem = ActorSystem.create("ActorSystem")

    //Materializer
    implicit val materializer = ActorMaterializer.apply()

    //The second parameterized type is secondary information
    val source: Source[Int, NotUsed] = Source(1 to 100)

    val flow = Flow[Integer].map(_.toString)
  }
}