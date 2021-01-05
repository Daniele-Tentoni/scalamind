package it.mm

case class MastermindPlayer(name: String, points: Int)

/**
 * This will become a Monad. As that, it must pass those Tests:
 *     left-identity law:
    unit(x).flatMap(f) == f(x)
    right-identity law:
    m.flatMap(unit) == m
    associativity law:
    m.flatMap(f).flatMap(g) == m.flatMap(x ⇒ f(x).flatMap(g))
 * @tparam T
 */
case class Lobby[T](items: Seq[T]) {
  def isEmpty: Boolean = items.isEmpty

  def +(i: T): Lobby[T] = ???

  def ++(l: Lobby[T]): Lobby[T] = copy(items = items ++ l.items)

  def -(i: T): Lobby[T] = ???

  def flatMap[R](f: T => Lobby[R]): Lobby[R] = ???

  def map(): Lobby[T] = ???
}

object Lobby {

  def empty[T]: Lobby[T] = Lobby(Seq.empty[T])

  def unit[T](x: T): Lobby[T] = Lobby(Seq(x))
}
