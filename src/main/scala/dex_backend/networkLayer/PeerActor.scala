package dex_backend.networkLayer

import java.net.InetSocketAddress

import _root_.NetworkMessageProto.GeneralizedNetworkMessage
import akka.actor.{Actor, ActorRef, Props}
import akka.io.Tcp._
import akka.io.Tcp
import akka.util.ByteString
import dex_backend.networkLayer.NetworkMessages.{GeneralizedNetworkMessageProto, NetworkMessageProto}
import dex_backend.networkLayer.NetworkServer.MessageFromPeer

import scala.concurrent.ExecutionContextExecutor
import scala.util.Success
import scala.concurrent.duration._

class PeerActor(remoteAddress: InetSocketAddress, listener: ActorRef, networkServiceRef: ActorRef) extends Actor {

  context.watch(listener)

  implicit val ec: ExecutionContextExecutor = context.dispatcher

  override def postStop(): Unit = {
    println(s"Peer handler $self to $remoteAddress is destroyed.")
    listener ! Close
  }

  override def receive: Receive = defaultLogic
    .orElse(readDataFromRemote)
    .orElse(writeDataToRemote)

  def defaultLogic: Receive = {
    case cc: ConnectionClosed =>
      println(s"Connection closed to $remoteAddress cause ${cc.getErrorCause}.")
      context.stop(self)
    case fail@CommandFailed(cmd: Tcp.Command) =>
      println(s"Failed to execute command : $cmd cause ${fail.cause}.")
      listener ! ResumeReading
  }

  def readDataFromRemote: Receive = {
    case Received(data) => GeneralizedNetworkMessageProto.fromProto(data) match {
      case Success(message) =>
        println(s"Got new network message ${message.messageName} from $remoteAddress.")
        networkServiceRef ! MessageFromPeer(remoteAddress, message)
      case _ => println(s"Can not parse received message!")
    }
      listener ! ResumeReading
  }

  def writeDataToRemote: Receive = {
    case message: NetworkMessageProto =>
      val serializedMessage: GeneralizedNetworkMessage = GeneralizedNetworkMessageProto.toProto(message)
      listener ! Write(ByteString(serializedMessage.toByteArray))
      println(s"Sent ${message.messageName} to $remoteAddress")
    case _ =>
      println(s"Got something strange on PeerActor connected to $remoteAddress")
  }
}

object PeerActor {

  case object InnerPong

  def props(remoteAddress: InetSocketAddress, listener: ActorRef, server: ActorRef): Props =
    Props(new PeerActor(remoteAddress, listener, server))
}