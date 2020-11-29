package it.mm

import akka.actor.{Actor, ActorSystem}
import it.mm.actors.JudgeActor

object Mastermind extends App {
  println("Working on Mastermind actor distributed system.")
  val system = ActorSystem("Mastermind")
  println("Input player number")
  /*Utils.readInteger.onComplete {
    case Success(Some(p)) =>
      println("Input turn timer")
      Utils.readInteger.onComplete {
        case Success(Some(t)) =>
          val judge = system.actorOf(JudgeActor.props)
          judge ! JudgeActor.Create(p)
        case Failure(_) | Success(None) => println("Must be an integer")
      }
    case Failure(_) | Success(None) => println("Must be an integer")
  }
   */
  val judge = system.actorOf(JudgeActor.props, "Judge")
  judge ! JudgeActor.Create(2)

  implicit class RichActor(a: Actor) {

    /**
      * Print in the console a formatted string.
      *
      * @param m Message to print.
      */
    def log(m: String): Unit =
      System.out.println(f"[${a.self.path.name}] Log: $m")

    /**
      * Print in error channel a message.
      *
      * @param m Message to print.
      */
    def error(m: String): Unit =
      System.err.println(f"[${a.self.path.name}] Error: $m")

    /**
      * Stop execution context of the actor.
      */
    def stop() {
      this.log("Goodbye!")
      // game ! Message.Leave
      a.context.stop(a.self)
    }
  }

}