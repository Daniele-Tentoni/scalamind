package it.mm.actors

import akka.actor.{Actor, ActorRef, Props, Stash}
import it.mm.actors.PlayerActor._
import it.mm.Mastermind.RichActor

import scala.util.Random

object PlayerActor {

  /**
    * Identify the game judge.
    */
  case class Hi()

  /**
    * Start the turn of the player referenced.
    *
    * @param players list of current players.
    */
  case class Start(players: Seq[ActorRef])

  /**
    * Tell to player to wake up and take turn.
    */
  case class Wake()

  /**
    * Tell to player to guess his secret.
    *
    * @param i secret guess.
    */
  case class Guess(i: Int)

  /**
    * Tell to player the result of his guess.
    *
    * @param r boolean result.
    */
  case class Result(r: Boolean)

  /**
    * Tell to player that a player have won.
    *
    * @param p winner reference.
    */
  case class Won(p: ActorRef)

  def props: Props = Props(new PlayerActor())
}

class PlayerActor extends Actor with Stash {
  override def receive: Receive = initializing

  private[this] def initializing: Receive = {
    case Hi() =>
      this.log(f"Received Hi from ${sender.path.name}")
      val secret: Int = Random.nextInt(10)
      this.log(f"Generate $secret number")
      sender ! JudgeActor.Ready()
      this.log(f"Going in waiting")
      context.become(waiting(sender, secret))

    case a =>
      this.error(f"Received $a from ${sender.path} while in initializing")
  }

  def waiting(j: ActorRef, secret: Int): Receive = {
    case Start(players) if sender.equals(j) =>
      this.log(
        f"Received Start(${players.map(_.path.name)}) from ${sender.path.name}"
      )
      // Pick only other players.
      val others = players.filterNot(_ == self)
      context.become(idle(others, j, secret))
      this.log(f"Became idle")

    case a =>
      this.log(f"Received $a from ${sender.path} while in waiting")
  }

  def idle(players: Seq[ActorRef], j: ActorRef, secret: Int): Receive = {
    case Wake() =>
      this.log(f"Woke up from ${sender.path.name}")
      players.headOption match {
        case Some(p) =>
          val i = Random.nextInt(10)
          this.log(f"Send Guess($i) to ${p.path.name}")
          p ! Guess(i)
          unstashAll()
          context.become(trying(p, players, j, secret))
        case None =>
          this.log("No more players in game")
          // Player had won
          j ! JudgeActor.Won()
      }

    case Guess(i) =>
      // After reply to a guess, I don't have to change my state.
      this.log(f"Received Guess($i) from ${sender.path.name}")
      val res = secret.equals(i)
      this.log(f"Reply with $res")
      sender ! Result(res)

    case Won(p) =>
      if (p.equals(self)) {
        this.log("You are the winner!")
      } else {
        this.log(f"${p.path.name} is the winner!")
      }
      // I can softly stop my execution.
      this.stop()

    case a =>
      this.error(f"Received $a from ${sender.path.name} while in idle")
      stash()
      this.log("Stashed message")
  }

  def trying(
    p: ActorRef,
    players: Seq[ActorRef],
    j: ActorRef,
    secret: Int
  ): Receive = {
    case Result(r) if sender.equals(p) =>
      this.log(f"Received Result($r) from ${sender.path.name}")
      val remaining = if (r) {
        players.tail
      } else {
        players
      }
      j ! JudgeActor.EndTurn()
      context.become(idle(remaining, j, secret))

    case a =>
      this.error(f"Received $a from ${sender.path.name} while in trying")
  }

  private[this] def stopWithError(t: Throwable): Unit = {
    this.error(f"An error occurred with $t")
    // game ! Message.Leave
    context.stop(self)
  }

  /*private[this] def askAndThen[T](ask: Future[T])(then: T => Any): Unit = {
    context become idle
    ask.onComplete {
      case Success(value) => then(value)
      case Failure(t: Throwable) => stopWithError(t)
    }
  }

  private[this] def askForMessage = {
    println("Pick a number")
    askAndThen(MastermindUtils.readCommand) {
      case Some(message) =>
      // game ! message
      // context.become(waiting)
      case None => stop
    }
  }*/
}
