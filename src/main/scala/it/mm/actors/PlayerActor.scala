package it.mm.actors

import akka.actor.{Actor, ActorRef, Props, Stash}
import it.mm.actors.PlayerActor._
import it.mm.Mastermind.RichActor
import it.mm.actors.models.PlayerState

object PlayerActor {

  def props: Props = Props(new PlayerActor())

  sealed case class Secret(s: Seq[Int])

  /**
    * Identify the game judge.
    * @param s sequence Actor Ref.
    */
  case class Hi(s: ActorRef)

  /**
    * Start the turn of the player referenced.
    * @param players list of current players.
    */
  case class Start(players: Seq[ActorRef])

  /**
    * Tell to player to wake up and take turn.
    */
  case class Wake()

  /**
    * Tell to player to guess his secret.
    * @param i secret guess.
    */
  case class Guess(i: Seq[Int])

  /**
    * Tell to player the result of his guess.
    * @param r boolean result.
    */
  case class Result(r: Boolean)

  /**
    * Tell to player that a player have won.
    * @param p winner reference.
    */
  case class Won(p: ActorRef)
}

class PlayerActor extends Actor with Stash {
  override def receive: Receive = initializing

  /**
    * Actor state after required a secret to sequence actor.
    * @param j judge actor reference.
    * @param s sequence actor reference.
    * @return receive.
    */
  def extracting(j: ActorRef, s: ActorRef): Receive = {
    case Secret(seq) if seq.nonEmpty && sender == s =>
      this.log("Secret received, send ready to Judge and waiting.")
      context.become(waiting(j, s, seq))
      j ! JudgeActor.Ready()
      unstashAll()
    case a =>
      this.error(s"Received $a while in extracting state.")
      stash()
  }

  def waiting(j: ActorRef, s: ActorRef, secret: Seq[Int]): Receive = {
    case Start(players) if sender.equals(j) =>
      this.log(
        f"Received Start(${players.map(_.path.name)}) from ${sender.path.name}"
      )
      // Pick only other players.
      val others = players.filterNot(_ == self)
      val state = PlayerState(others, j, s, secret)
      context.become(idle(state))
      this.log(f"Became idle")

    case a =>
      this.log(f"Received $a from ${sender.path} while in waiting")
  }

  def trying(p: ActorRef, state: PlayerState): Receive = {
    case Result(r) if sender == p =>
      this.log(f"Received Result($r) from ${sender.path.name}")
      val newState = if (r) {
        state.success(p)
      } else {
        state
      }
      context.become(idle(newState))
      state.judge ! JudgeActor.EndTurn()

    case a =>
      this.error(f"Received $a from ${sender.path.name} while in trying")
  }

  def idle(state: PlayerState): Receive = {
    case Wake() =>
      this.log(f"Woke up from ${sender.path.name}")
      state.players.headOption match {
        case Some(p) =>
          this.log("Require to sequence actor a sequence.")
          // Require a secret to sequence actor.
          context.become(thinking(p, state))
          state.sequence ! SequenceActor.Extract()
        case None =>
          this.log("No more players in game")
          // Player had won
          state.judge ! JudgeActor.Won()
      }

    case Guess(i) =>
      // After reply to a guess, I don't have to change my state.
      this.log(f"Received Guess($i) from ${sender.path.name}")
      val res = state.secret.equals(i)
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

  /**
    * State after secret require from sequence actor to use for a Guess to another
    * player.
    * @param p player to guess to.
    * @param state actual player state.
    * @return
    */
  def thinking(
    p: ActorRef,
    state: PlayerState
  ): Receive = {
    // Check if sender is Sequence Actor.
    case Secret(s) if sender == state.sequence =>
      this.log(s"Received secret: $s from ${sender.path.name}")
      this.log(f"Send Guess($s) to ${p.path.name}")
      context.become(trying(p, state))
      p ! Guess(s)
      unstashAll()
    case a =>
      this.error(s"Received $a from ${sender.path.name} while thinking.")
      stash()
  }

  private[this] def initializing: Receive = {
    // Judge say Hi to Player to make him send Extract message to Sequence actor.
    case Hi(s) =>
      this.log(
        f"Received sequence actor: ${s.path.name} from ${sender.path.name}"
      )
      this.log("Going to extracting state")
      // Switch state remembering judge and sequence actor references.
      context.become(extracting(sender, s))
      this.log(f"Send secret request to Sequence Actor.")
      //sender ! JudgeActor.Ready()
      s ! SequenceActor.Extract()
      unstashAll()

    case a =>
      this.error(f"Received $a from ${sender.path} while in initializing")
      stash()
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
