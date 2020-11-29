package it.mm.model

import akka.actor.ActorRef

sealed case class Player(name: String, ref: ActorRef)
