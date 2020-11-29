package it.mm

import akka.actor.ActorRef
import it.mm.model.Player

sealed trait Lobby[T <: Player] {
  def isEmpty: Boolean

  def players(n: Int): Seq[String]

  def refs(): Seq[ActorRef]

  def +(item: T): Lobby[T]

  def -(id: String): Lobby[T]
}

object Lobby {

  private[this] case class PlayersLobby[T <: Player](items: Seq[T])
      extends Lobby[T] {
    override def isEmpty: Boolean = items.isEmpty

    override def players(n: Int): Seq[String] = items.map(_.name)

    override def refs(): Seq[ActorRef] = items.map(_.ref)

    override def +(item: T): Lobby[T] = copy(items = items :+ item)

    override def -(n: String): Lobby[T] =
      copy(items = items.filterNot(f => f.name == n))
  }

  def empty: Lobby[Player] = PlayersLobby(Seq.empty[Player])
}
