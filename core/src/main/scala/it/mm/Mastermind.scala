package it.mm

import akka.actor.{Actor, ActorSystem}
import it.mm.actors.JudgeActor

import scala.Console.{GREEN_B, RED_B, RESET, YELLOW_B}

object Mastermind extends App {
  println("Working on Mastermind actor distributed system.")
  val system = ActorSystem("Mastermind")
  println("Run judge actor")
  val judge = system.actorOf(JudgeActor.props, "Judge")

  /**
    * Expand Actor functionalities with log actions.
    * @param a actor to extend.
    */
  implicit class RichActor(a: Actor) {

    /**
      * Print in error channel a message.
      * @param m Message to print.
      */
    def error(m: String): Unit =
      Console.err.println(s"$RED_B[${a.self.path.name}]$RESET Error: $m")

    /**
      * Stop execution context of the actor.
      */
    def stop() {
      this.log("Goodbye!")
      // game ! Message.Leave
      a.context.stop(a.self)
    }

    /**
      * Print in the console a formatted string.
      * @param m Message to print.
      */
    def log(m: String): Unit =
      Console.out.println(f"$YELLOW_B[${a.self.path.name}]$RESET Log: $m")

    /**
      * Print in the console an answer made by the actor.
      * @param m answer to ask.
      */
    def ask(m: String): Unit =
      Console.out.println(f"$GREEN_B[${a.self.path.name}]$RESET Ask: $m")
  }
}
