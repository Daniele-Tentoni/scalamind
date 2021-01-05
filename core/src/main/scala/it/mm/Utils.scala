package it.mm

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Utils {
  val MastermindSystem = "Mastermind"
  val JudgeName = "Judge"

  /** Read on integer input from console. This input must be converted before from string to int.
    * @return input int.
    */
  def readInteger: Future[Either[String, Int]] =
    readResponse.map { s =>
      try {
        // TODO: This can become an Either.
        Right(s.toInt)
      } catch {
        case _: Throwable => Left(s)
      }
    }

  /** Read a boolean input from console. This input must be mapped before from string to boolean.
    * @return input boolean.
    */
  def readBoolean: Future[Boolean] =
    readResponse.map(s =>
      s.toLowerCase() match {
        case "y" | "yes" | "1" | "" => true
        case _                      => false
      }
    )

  /*def readCommand: Future[Option[Message]] = {
    readResponse map (s => {
      try {
        Some(Message(s))
      } catch {
        case _: Throwable => None
      }
    })
  }*/

  /** Read a generic input from console.
    * @return input generic string.
    */
  def readResponse: Future[String] = Future {
    scala.io.StdIn.readLine()
  }
}
