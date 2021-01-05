package it.mm.actors

import akka.actor.{ActorKilledException, ActorSystem, Kill}
import akka.testkit.{EventFilter, ImplicitSender, TestKit}
import com.typesafe.config.ConfigFactory
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.BeforeAndAfterAll

class SequenceActorSpec
    extends TestKit(
      ActorSystem(
        "SequenceActor",
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

  "A Sequence Actor" when {
    val seq = system.actorOf(SequenceActor.props)
    "in receive state" should {
      "discard Extract messages" in {
        EventFilter.warning(occurrences = 2).intercept {
          seq ! SequenceActor.Extract()
        }
      }
      "discard all other messages" in {
        // Declare a message for test.
        case object SomeMessage
        EventFilter.error(occurrences = 1).intercept {
          seq ! SomeMessage
        }
      }
      "accept config messages" in {
        val l = 1
        EventFilter
          .info(start = s"Config to", occurrences = 1)
          .intercept {
            seq ! SequenceActor.Config(l)
          }
      }
    }
    "in working state" should {
      "accept extract messages" in {
        EventFilter
          .info(start = s"Received Extract command from ", occurrences = 1)
          .intercept {
            seq ! SequenceActor.Extract()
          }
      }
    }
    "at the end of the work" should {
      "actor get killed" in {
        try {
          EventFilter[ActorKilledException](occurrences = 1).intercept {
            seq ! Kill
          }
        } finally {
          TestKit.shutdownActorSystem(system)
        }
      }
    }
  }
}
