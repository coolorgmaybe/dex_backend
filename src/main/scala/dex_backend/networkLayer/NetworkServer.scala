package dex_backend.networkLayer

import java.net.{InetAddress, InetSocketAddress}

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.io.{IO, Tcp}
import akka.io.Tcp._
import dex_backend.networkLayer.NetworkMessages.{MatchedOrderNetworkMessage, NetworkMessageProto, OrderRequestNetworkMessage}
import dex_backend.networkLayer.NetworkServer.{MessageFromPeer, TestedPing}

import scala.concurrent.duration._
import scala.concurrent.ExecutionContextExecutor

class NetworkServer extends Actor {

  implicit val system: ActorSystem = context.system
  implicit val ec: ExecutionContextExecutor = context.dispatcher

  val selfAddress: InetSocketAddress = new InetSocketAddress("0.0.0.0", 9101)

  var connectedPeer: Map[InetSocketAddress, (ActorRef, String)] = Map.empty[InetSocketAddress, (ActorRef, String)]

  IO(Tcp) ! Bind(self, selfAddress)

  //system.scheduler.schedule(20.seconds, 20.seconds, self, TestedPing)

  override def receive: Receive = {
    case Bound(localAddress) =>
      println(s"Local app was successfully bound to $localAddress!")

    case CommandFailed(_: Bind) =>
      println(s"Failed to bind to $selfAddress.")
      context.stop(self)

    case Connected(remote, _) =>
      val handler: ActorRef = context.actorOf(PeerActor.props(remote, sender, self))
      println(s"Got new connection from $remote. Creating handler: $handler.")
      sender ! Register(handler)
      sender ! ResumeReading
      connectedPeer = connectedPeer.updated(remote, (handler, ""))

    case CommandFailed(c: Connect) =>
      connectedPeer -= c.remoteAddress
      println(s"Failed to connect to : ${c.remoteAddress}")

    case MessageFromPeer(remote, message) =>
      message match {
        case msg@OrderRequestNetworkMessage(assetId, clientId, direction, price, volume) =>
          println(s"${msg.toString} from $remote")
          connectedPeer = connectedPeer.find(peer => peer._1 == remote && peer._2._2.isEmpty) match {
            case Some(_) => connectedPeer.updated(remote, (sender, clientId))
            case _       => connectedPeer
          }
          println(s"Updated peer collection ${connectedPeer.mkString(",")}.")
          sender ! MatchedOrderNetworkMessage("TESTED", 999, 666, "TESTED")
        case msg@MatchedOrderNetworkMessage(orderId, price, volume, matchedAddressId) =>
          println(s"${msg.toString} from $remote")
          sender ! MatchedOrderNetworkMessage("TESTED999", 10000000, 99999999, "TESTED666")
      }

    case TestedPing =>
      val message: NetworkMessageProto = MatchedOrderNetworkMessage("testedPing", 100, 1000, "testedPing")
      connectedPeer.foreach { peer =>
        println(s"Sent {$message} to: ${peer._1}")
        peer._2._1 ! message
      }

    case _ =>
  }

}

object NetworkServer {

  case class MessageFromPeer(peer: InetSocketAddress, message: NetworkMessageProto)

  case object TestedPing

  def props: Props = Props(new NetworkServer)
}