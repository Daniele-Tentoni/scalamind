package it.mm

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Utils {
  val MastermindSystem = "Mastermind"
  val JudgeName = "Judge"

  def readResponse: Future[String] = Future {
    scala.io.StdIn.readLine()
  }

  def readInteger: Future[Option[Int]] =
    readResponse.map(s => {
      try {
        Some(s.toInt)
      } catch {
        case _: Throwable => None
      }
    })

  /*def readCommand: Future[Option[Message]] = {
    readResponse map (s => {
      try {
        Some(Message(s))
      } catch {
        case _: Throwable => None
      }
    })
  }*/

  def readBoolean: Future[Boolean] =
    readResponse.map(s =>
      s.toLowerCase() match {
        case "y" | "yes" | "1" | "" => true
        case _                      => false
      }
    )
}
