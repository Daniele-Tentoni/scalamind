package it.mm.actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Stash}
import it.mm.actors.JudgeActor._
import it.mm.Mastermind.RichActor
import it.mm.actors.models.Message
import it.mm.Utils

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Random, Success}

object JudgeActor {

  def props: Props = Props(new JudgeActor())

  /** Tell to judge to create a game with players.
    * @param p number of players integrated.
    */
  case class Number(p: Int) extends Message

  /** Tell to judge to set the timer of the game at t.
    * @param t milliseconds integer.
    */
  case class Timer(t: Int) extends Message

  /** Tell to judge to set string length of players to l.
    * @param l string length integer.
    */
  case class StringLength(l: Int) extends Message

  /** Confirm that a player is ready.
    */
  case class Ready() extends Message

  /** Tell to judge to start a game.
    */
  case class Start(players: Seq[ActorRef]) extends Message

  /** Tell to judge to end turn and wake another player.
    */
  case class EndTurn() extends Message

  /** Tell to judge that a player had won.
    */
  case class Won() extends Message
}

class JudgeActor extends Actor with Stash with ActorLogging {
  override def receive: Receive = initializing(None, None, None)

  /** In this state, Judge Actor ask to user some parameters before start the
    * execution of the game. In my case of study, the workflow is fixed: before
    * ask for number of players, then for turn timer and finally for string
    * length.
    * @param numberOfPlayers number of player configured.
    * @param timeToLive turn timer configured.
    * @param stringLength string length configured.
    * @return actor state.
    */
  def initializing(
    numberOfPlayers: Option[Int],
    timeToLive: Option[Int],
    stringLength: Option[Int]
  ): Receive = {
    // User have message the number of players.
    case Number(n) if n > 1 =>
      // User have input a valid player number.
      log.info("Received player number message")
      this.log("Now I can ask for timer")
      context.become(initializing(Some(n), timeToLive, stringLength))
      askTimer()

    // User have message the timer for turns.
    case Timer(t) if t >= 0 =>
      // User have input a valid timer value.
      log.info(f"Received timer message")
      this.log("Now I can ask for string length.")
      context.become(initializing(numberOfPlayers, Some(t), stringLength))
      askString()

    // User have message the length of sequence.
    case StringLength(l) if l > 0 =>
      // User have input a valid string length.
      log.info(f"Received string length message")
      this.log("Now I can generate players.")
      val p = numberOfPlayers.map(i => i).getOrElse(2)
      val t = timeToLive.map(i => i).getOrElse(100)
      // val l = stringLength.map(i => i).getOrElse(4)
      startGame(p, t, l)

    // Unknown message.
    case a =>
      log.error(f"Received $a from ${sender.path.name} while in initializing")
      stash()
      this.log("Stashed message")
  }

  def starting(n: Int, t: Int, players: Seq[ActorRef]): Receive = {
    case Ready() if !players.contains(sender) =>
      log.info(s"Received ready message from ${sender.path.name}")
      val o = n - 1
      val readies = sender +: players
      if (o.equals(0)) {
        // All players are ready.
        readies.foreach(_ ! PlayerActor.Start(readies))
        readies.headOption match {
          case Some(value) =>
            value ! PlayerActor.Wake()
            this.log("Wait for turn end")
            context.become(waiting(readies, value))
          case None =>
            this.error("Players is empty")
        }
      } else {
        this.log("Wait for other players")
        context.become(starting(o, t, readies))
      }

    case a =>
      this.error(f"Received $a from ${sender.path.name} while in starting")
      stash()
      this.log("Stashed message")
  }

  def waiting(players: Seq[ActorRef], p: ActorRef): Receive = {
    case EndTurn() if sender().equals(p) =>
      this.log(f"${sender.path.name} of ${players.map(_.path.name)} ended turn")
      Random.shuffle(players.filterNot(_.equals(p))).headOption match {
        case Some(next) =>
          next ! PlayerActor.Wake()
          context.become(waiting(players, next))
        case None =>
          this.error(f"No other players to call")
      }

    case Won() =>
      this.log(f"Received Won from ${sender.path.name}")
      players.foreach(_ ! PlayerActor.Won(sender))
      this.stop()

    case a =>
      this.error(f"Received $a from ${sender.path.name} while in waiting")
  }

  override def preStart(): Unit = {
    this.log("Welcome to Mastermind! I'm the judge.")
    // This instruction start the workflow for initialization.
    // TODO: This maybe configurable.
    askPlayers()
  }

  /** Ask user for number of players to generate.
    * Send the result to JudgeActor with Number(n) message.
    */
  private[this] def askPlayers(): Unit = {
    def doPrompt(): Unit = Utils.readInteger.onComplete {
      case Success(Right(value)) if value > 0 =>
        println(f"Approved $value players")
        self ! Number(value)
      case Success(Left(value)) =>
        println(s"Invalid input $value")
        doPrompt()
      case _ =>
        println(f"Invalid input, please try again.")
        doPrompt()
    }

    this.ask("Please enter the number of players")
    doPrompt()
  }

  /** Ask to user the timer for this match.
    * This instance a Future that complete with user input and send a message to judge actor with data.
    */
  private[this] def askTimer(): Unit = {
    def doPrompt(): Unit = Utils.readInteger.onComplete {
      case Success(Right(value)) if value >= 0 =>
        println(f"Approved $value timer")
        self ! Timer(value)
      case Success(Left(value)) =>
        println(s"Invalid input $value")
        doPrompt()
      case _ =>
        println(f"Invalid input, please try again.")
        doPrompt()
    }

    this.ask("Please enter player timer. 0 means no timer.")
    doPrompt()
  }

  /** Ask to user the string length for this match.
    * This instance a Future that complete with user input and send a message to judge actor with data.
    */
  private[this] def askString(): Unit = {
    def doPrompt(): Unit = Utils.readInteger.onComplete {
      case Success(Right(value)) if value > 0 =>
        println(f"Approved for $value string length.")
        self ! StringLength(value)
      case Success(Left(value)) =>
        println(s"Invalid input $value")
        doPrompt()
      case _ =>
        println("Invalid input, please try again.")
        doPrompt()
    }

    this.ask("Please enter player string length. Must be a positive integer.")
    doPrompt()
  }

  /** Generate and send an invitation to all players.
    * @param players number of players to generate.
    * @param timer timer before turn ending.
    * @param length length of the string to generate.
    */
  private[this] def startGame(players: Int, timer: Int, length: Int): Unit = {
    val sequence = context.actorOf(SequenceActor.props, "Sequence")
    sequence ! SequenceActor.Config(length)
    val p = welcomePlayers(players, sequence)
    this.log("Going in starting")
    context.become(starting(players, timer, Seq.empty[ActorRef]))
    this.log("Unstashed all messages")
    unstashAll()
  }

  /** Generate and say Hi to players.
    * @param p number of players to generate.
    * @param s sequence actor to send to players.
    * @return sequence of players.
    */
  private[this] def welcomePlayers(p: Int, s: ActorRef): Seq[ActorRef] =
    (0 until p).map { i =>
      val player = context.actorOf(PlayerActor.props, f"Player_$i")
      this.log(f"Created player ${player.path.name}")
      player ! PlayerActor.Hi(s)
      this.log(f"Sent Hi to ${player.path.name}")
      player
    }

}
