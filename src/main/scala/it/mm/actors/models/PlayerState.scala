package it.mm.actors.models

import akka.actor.ActorRef

/**
  * Group some frequently required fields of Player.
  * @param players other players.
  * @param judge judge actor reference.
  * @param sequence sequence reference.
  * @param secret secret reference.
  */
sealed case class PlayerState(
  players: Seq[ActorRef],
  judge: ActorRef,
  sequence: ActorRef,
  secret: Seq[Int]
) {

  /**
    * Remove a player that secret was successful guessed from player.
    * @param p player guessed.
    * @return new player state.
    */
  def success(p: ActorRef): PlayerState =
    copy(players = players.filterNot(r => r == p))
}
