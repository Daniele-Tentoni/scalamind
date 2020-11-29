package it.mm.actors

import akka.actor.{Actor, ActorRef, Props}

object LobbyActor {

  /**
    * Send this message to add a player to the lobby.
    *
    * @param p player to add.
    */
  case class Add(p: ActorRef)

  /**
    * Tell to Lobby that a player is ready.
    * @param p player to make ready.
    */
  case class Ready(p: ActorRef)

  def props: Props = Props(new LobbyActor())
}

class LobbyActor extends Actor {
  override def receive: Receive = idle

  private[this] def idle: Receive = Actor.emptyBehavior
}
