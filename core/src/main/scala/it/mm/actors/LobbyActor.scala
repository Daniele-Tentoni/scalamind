package it.mm.actors

import akka.actor.{Actor, ActorRef, Props}
import it.mm.actors.models.Message

object LobbyActor {

  /**
   * Tell to lobby that j is a judge of a game.
   *
   * @param j judge reference.
   */
  case class Hi(j: ActorRef) extends Message

  /**
   * Send this message to add a player to the lobby.
   *
   * @param p player to add.
   */
  case class Add(p: ActorRef) extends Message

  /**
   * Tell to Lobby that a player is ready.
   *
   * @param p player to make ready.
   */
  case class Ready(p: ActorRef) extends Message

  def props: Props = Props(new LobbyActor())
}

class LobbyActor extends Actor {
  override def receive: Receive = idle

  private[this] def idle: Receive = Actor.emptyBehavior
}
