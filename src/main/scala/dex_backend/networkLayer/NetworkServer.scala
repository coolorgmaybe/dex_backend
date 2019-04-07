package dex_backend.networkLayer

import java.net.{InetAddress, InetSocketAddress}
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.io.{IO, Tcp}
import akka.io.Tcp._
import dex_backend.networkLayer.NetworkMessages.{MatchedOrderNetworkMessage, NetworkMessageProto, OrderRequestNetworkMessage, TradeDirectiveNetworkMessage}
import dex_backend.networkLayer.NetworkServer.{MessageFromPeer, TestedPing}
import dex_backend.trading.{BestExecutionMatchingEngine, TradeDirective}
import dex_backend.trading.order.{Order, OrderBook, OrderDirection, OrderStatus}
import scala.collection.mutable
import scala.concurrent.duration._
import scala.concurrent.ExecutionContextExecutor

class NetworkServer extends Actor {

  implicit val system: ActorSystem = context.system
  implicit val ec: ExecutionContextExecutor = context.dispatcher

  val selfAddress: InetSocketAddress = new InetSocketAddress("0.0.0.0", 9111)

  var connectedPeer: Map[InetSocketAddress, (ActorRef, String)] = Map.empty[InetSocketAddress, (ActorRef, String)]

  var orderBook: OrderBook = OrderBook.empty("wood", "stone")

  val matchingEngine: BestExecutionMatchingEngine = new BestExecutionMatchingEngine

  IO(Tcp) ! Bind(self, selfAddress)

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
      //connectedPeer = connectedPeer.updated(remote, (handler, ""))

    case CommandFailed(c: Connect) =>
      connectedPeer -= c.remoteAddress
      println(s"Failed to connect to : ${c.remoteAddress}")

    case MessageFromPeer(remote, message) =>
      message match {
        case msg@OrderRequestNetworkMessage(assetId, exchangeAssetId, clientId, direction, price, volume) =>
          println(s"Got ${msg.toString} from $remote with clientId: $clientId.")
          connectedPeer = connectedPeer.find(peer => peer._1 == remote && peer._2._2.isEmpty) match {
            case Some(_) => connectedPeer.updated(remote, (sender, clientId))
            case _ => connectedPeer
          }
          val order: Order = Order(
            assetId,
            exchangeAssetId,
            clientId,
            direction,
            price,
            volume,
            System.currentTimeMillis(),
            OrderStatus.Available)

          orderBook = orderBook.add(order)

          connectedPeer = connectedPeer.updated(remote, (sender, clientId))

          val directives: (List[TradeDirective], OrderBook) = matchingEngine.doMatch(orderBook)
          orderBook = directives._2
          if (directives._1.nonEmpty) {
            println(s"Got new matched directive!")
            directives._1.foreach { d =>
              println(s"Matched directive is: assetId -> ${d.assetId}, counterPartyClientId -> ${d.counterPartyClientId}," +
                s" price -> ${d.price}, targetOrderOwnerClientId -> ${d.targetOrderOwnerClientId}, " +
                s"volume -> ${d.volume}.")
            }
            directives._1.foreach { k =>
              val clients = connectedPeer.filter(x => x._2._2 == k.targetOrderOwnerClientId || x._2._2 == k.counterPartyClientId)
              clients.foreach { m =>
                m._2._1 ! TradeDirectiveNetworkMessage(
                  k.targetOrderOwnerClientId,
                  k.counterPartyClientId,
                  k.assetId,
                  k.volume,
                  k.price,
                  k.targetOrderId,
                )
              }
            }

          }

        case msg@MatchedOrderNetworkMessage(orderId, price, volume, matchedAddressId) =>
          println(s"${msg.toString} from $remote")
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