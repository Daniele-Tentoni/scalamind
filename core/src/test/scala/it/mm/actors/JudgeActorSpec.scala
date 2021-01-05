package it.mm.actors

import akka.actor.ActorSystem
import akka.testkit.{EventFilter, ImplicitSender, TestKit}
import com.typesafe.config.ConfigFactory
import org.scalatest.matchers.should.Matchers
import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpecLike

class JudgeActorSpec
  extends TestKit(
    ActorSystem(
      "JudgeActor",
      ConfigFactory
        .parseString("""
          akka.loggers = ["akka.testkit.TestEventListener"]
          """)
    )
  )
    with ImplicitSender
    with AnyWordSpecLike
    with Matchers
    with BeforeAndAfterAll {

  override def afterAll: Unit =
    TestKit.shutdownActorSystem(system)

  "A Judge Actor" when {
    "in initializing state" should {
      val judge = system.actorOf(JudgeActor.props)
      "log info when receive number message" in {
        EventFilter.info(start = "Received player number message", occurrences = 1).intercept {
          judge ! JudgeActor.Number(2)
        }
      }
      "log info when receive timer message" in {
        EventFilter.info(start = "Received timer message", occurrences = 1).intercept {
          judge ! JudgeActor.Timer(2)
        }
      }
      "log error when receive another messages" in {
        EventFilter.error(start = "Received Ready() from", occurrences = 1).intercept {
          judge ! JudgeActor.Ready()
        }
      }
      "log info when receive length message" in {
        EventFilter.info(start = "Received string length message", occurrences = 1).intercept {
          judge ! JudgeActor.StringLength(2)
        }
      }
    }
  }
}
