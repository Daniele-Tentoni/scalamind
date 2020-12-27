package it.mm.actors

import akka.actor.{
  Actor,
  ActorLogging,
  ActorRef,
  PoisonPill,
  Props,
  Stash,
  Terminated
}
import it.mm.actors.models.Message
import it.mm.actors.SequenceActor.{Config, Extract}
import it.mm.Mastermind.RichActor

import scala.concurrent.duration.DurationInt
import scala.util.Random

object SequenceActor {
  def props: Props = Props(new SequenceActor())

  /**
    * Say to sequence actor the sequence length for the game.
    * @param l sequence length.
    */
  sealed case class Config(l: Int) extends Message
  sealed case class Extract() extends Message
}

class SequenceActor extends Actor with Stash with ActorLogging {

  import context.dispatcher

  /**
    * First state for Sequence actor. Wait for a message from judge actor to
    * set his sequence length value for future Extract requests.
    * @return receive.
    */
  override def receive: Receive = {
    // Config sequence length.
    case Config(l) if l > 0 =>
      // Watch judge to receive terminated message.
      context.watch(sender)
      this.log(s"Config to $l received from ${sender.path.name}")
      context.become(working(sender, l))
      unstashAll()
    /*
     * Stash any extract message received. If players send that before
     * length configuration, stash the request for a better moment.
     */
    case Extract() =>
      this.error(s"Received extract message from ${sender.path.name}")
      this.error(s"Stash until Config(l) message from judge is received.")
      stash()
    // Other message doesn't be stashed.
    case a =>
      this.error(s"Received $a while in receive state.")
  }

  /**
    * Default state for a sequence actor. This mean that can accept Extract
    * messages. Can receive Terminated too to close itself with Poison Pill.
    * @param l length of sequence.
    * @return receive.
    */
  def working(j: ActorRef, l: Int): Receive = {
    // Extract a new sequence and report to user.
    case Extract() =>
      this.log(s"Received Extract command from ${sender.path.name}")
      // TODO: Change this number to l.
      val secret: Seq[Int] = (0 until 1).map(_ => Random.nextInt(10))
      sender ! PlayerActor.Secret(secret)

    // Anyone else in context is terminated.
    case Terminated(ref) =>
      this.error(s"Received terminated message from ${sender.path.name}")
      this.error(s"It contains ${ref.path.name} ref.")
      context.system.scheduler.scheduleOnce(5.second) {
        this.error(s"I'm closing too with Poison pill")
        self ! PoisonPill
      }

  }

}
