import akka.actor.{ActorSystem, Props}
import com.xyzcorp.actors.SimpleActor
import org.scalatest.{FunSuite, Matchers}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

class ActorSpec extends FunSuite with Matchers {
  test(
    """Testing a plain simple actor""".stripMargin) {

    val system = ActorSystem("MyActorSystem")

    val simpleActor = system.actorOf(Props[SimpleActor],
                       "simpleActor")
    simpleActor ! "What's up?"

    Thread.sleep(2000)
    Await.result(system.terminate(), 10 seconds)
  }
}
