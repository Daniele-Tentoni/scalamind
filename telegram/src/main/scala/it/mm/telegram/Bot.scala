package it.mm.telegram

import akka.http.scaladsl.model.{HttpRequest, Uri}
import akka.http.scaladsl.Http
import akka.http.scaladsl.unmarshalling.Unmarshal
import com.bot4s.telegram.api.{AkkaTelegramBot, Webhook}
import com.bot4s.telegram.api.declarative.Commands
import com.bot4s.telegram.clients.AkkaHttpClient
import com.bot4s.telegram.methods.SendMessage
import com.bot4s.telegram.models.Message
import com.typesafe.config.ConfigFactory

import java.net.URLEncoder
import scala.concurrent.Future

class Bot(token: String)
    extends AkkaTelegramBot
    with Webhook
    with Commands[Future] {
  val conf = ConfigFactory.load()
  val tToken: String = conf.getString("telegram.token")
  override val port = 8443
  override val webhookUrl: String = "https://1d1ceb07.ngrok.io"
  val client = new AkkaHttpClient(token)
  val baseUrl = "http://api.mathjs.org/v1/?expr="

  override def receiveMessage(msg: Message): Future[Unit] =
    msg.text.fold(Future.successful(())) { text =>
      val url = baseUrl + URLEncoder.encode(text, "UTF-8")
      for {
        res <- Http().singleRequest(HttpRequest(uri = Uri(url)))
        if res.status.isSuccess()
        result <- Unmarshal(res).to[String]
        _ <- request(SendMessage(msg.source, result))
      } yield ()
    }
}
