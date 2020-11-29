package it.mm.actors

import akka.actor.{Actor, ActorRef, Props, Stash}
import it.mm.actors.JudgeActor.{Create, EndTurn, Ready, Won}
import it.mm.Mastermind.RichActor

import scala.util.Random

object JudgeActor {

  /**
    * Tell to judge to create a game with players.
    *
    * @param p number of players.
    */
  case class Create(p: Int)

  /**
    * Confirm that a player is ready.
    */
  case class Ready()

  /**
    * Tell to judge to start a game.
    */
  case class Start(players: Seq[ActorRef])

  /**
    * Tell to judge to end turn and wake another player.
    */
  case class EndTurn()

  /**
    * Tell to judge that a player had won.
    */
  case class Won()

  def props: Props = Props(new JudgeActor())
}

class JudgeActor extends Actor with Stash {
  override def receive: Receive = initializing()

  def initializing(): Receive = {
    case Create(n) =>
      this.log("Creating players")
      // val lobby: ActorRef = context.actorOf(LobbyActor.props)
      (0 until n).foreach { i =>
        val player = context.actorOf(PlayerActor.props, f"Player_$i")
        this.log(f"Created player ${player.path.name}")
        player ! PlayerActor.Hi()
        this.log(f"Sent Hi to ${player.path.name}")
      // lobby ! LobbyActor.Add(player)
      }
      this.log("Unstashed all messages")
      unstashAll()
      this.log("Going in starting")
      context.become(starting(n, Seq.empty[ActorRef]))

    case a =>
      this.error(f"Received $a from ${sender.path.name} while in initializing")
      stash()
      this.log("Stashed message")
  }

  def starting(n: Int, players: Seq[ActorRef]): Receive = {
    case Ready() if !players.contains(sender) =>
      this.log(s"Received ready message from ${sender.path.name}")
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
        context.become(starting(o, readies))
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
}
