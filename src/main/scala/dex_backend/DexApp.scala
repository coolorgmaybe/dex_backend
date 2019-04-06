package dex_backend

import akka.actor.ActorSystem
import dex_backend.networkLayer.NetworkServer

object DexApp extends App {

  val actorSystem = ActorSystem("systemAkka")

  val networkServiceActor = actorSystem.actorOf(NetworkServer.props)
}
