package it.mm

import org.scalatest.wordspec.AnyWordSpecLike

class TestLobby extends AnyWordSpecLike {
  "A lobby" when {
    val l0 = Lobby.empty[Int]
    val l1 = Lobby.unit(1)
    val l2 = Lobby(Seq(1, 2))
    "just created" should {
      "be a correct sequence" in {
        assertResult(Seq(1, 2))(l2.items)
      }
      "be a unit" in {
        assertResult(Seq(1))(l1.items)
      }
      "be empty" in {
        assertResult(Seq.empty[Int])(l0.items)
      }
    }
    "at merge" should {
      "be empty with two empties" in {
        assertResult(Lobby.empty[Int])(l0 ++ l0)
      }
      "have one element" in {
        assertResult(Lobby.unit(1))(l0 ++ l1)
        assertResult(Lobby.unit(1))(l1 ++ l0)
      }
      "have two elements" in {
        assertResult(Lobby(Seq(1, 1)))(l1 ++ l1)
      }
      "have two elements anywhere" in {
        assertResult(Lobby(Seq(1, 2)))(l0 ++ l2)
        assertResult(Lobby(Seq(1, 2)))(l2 ++ l0)
      }
      "have four elements" in {
        assertResult(Lobby(Seq(1, 2, 1, 2)))(l2 ++ l2)
      }
    }
  }
}
