package dex_backend.networkLayer

import NetworkMessageProto.GeneralizedNetworkMessage
import NetworkMessageProto.GeneralizedNetworkMessage.{InnerMessage, MatchedOrderProtoMessage, OrderRequestProtoMessage, TradeDirectiveProtoMessage}
import NetworkMessageProto.GeneralizedNetworkMessage.InnerMessage.{MatchedOrder => MO, OrderRequest => OR, TradeDirective => TD}
import akka.util.ByteString
import dex_backend.trading.order.OrderDirection
import dex_backend.trading.order.OrderDirection.{Buy, Sell}
import scala.util.Try

object NetworkMessages {

  sealed trait NetworkMessageProto {

    val messageName: String

    val messageId: Byte

    def toInnerMessage: InnerMessage

  }

  sealed trait NetworkMessageProtoSerializer[T] {

    def toProto(message: T): InnerMessage

    def fromProto(message: InnerMessage): Option[T]
  }

  case class OrderRequestNetworkMessage(assetId: String,
                                        exchangeAssetId: String,
                                        clientId: String,
                                        direction: OrderDirection,
                                        price: Long,
                                        volume: Long) extends NetworkMessageProto {

    override val messageName: String = "Order Request"

    override val messageId: Byte = OrderRequestNetworkMessage.messageId

    override def toInnerMessage: InnerMessage = OrderRequestSerializer.toProto(this)

    override def toString: String = s"$messageName - assetId: $assetId, clientId: $clientId, direction: $direction" +
      s" price: $price, volume: $volume."
  }

  object OrderRequestNetworkMessage {

    val messageId: Byte = 1: Byte
  }

  object OrderRequestSerializer extends NetworkMessageProtoSerializer[OrderRequestNetworkMessage] {

    override def toProto(order: OrderRequestNetworkMessage): InnerMessage = {
      val initialOrder = OrderRequestProtoMessage()
        .withAssetId(order.assetId)
        .withClientId(order.clientId)
        .withPrice(order.price)
        .withVolume(order.volume)
        .withExchangeAssetId(order.exchangeAssetId)
      OR(order.direction match {
        case Sell => initialOrder.withSell(1)
        case Buy => initialOrder.withBuy(2)
      })
    }

    override def fromProto(message: InnerMessage): Option[OrderRequestNetworkMessage] = message.orderRequest match {
      case Some(order) =>
        val status: OrderDirection = order.orderStatus.buy match {
          case Some(_) => Buy
          case _ => Sell
        }
        Some(OrderRequestNetworkMessage(
          order.assetId,
          order.exchangeAssetId,
          order.clientId,
          status,
          order.price,
          order.volume)
        )
      case _ => Option.empty[OrderRequestNetworkMessage]
    }
  }

  case class MatchedOrderNetworkMessage(orderId: String,
                                        price: Long,
                                        volume: Long,
                                        matchedAddressId: String) extends NetworkMessageProto {

    override val messageName: String = "Order Matched"
    override val messageId: Byte = MatchedOrderNetworkMessage.messageId

    override def toInnerMessage: InnerMessage = MatchedOrderSerializer.toProto(this)

    override def toString: String = s"$messageName - orderId: $orderId, price: $price, volume: $volume," +
      s" matchedAddressId: $matchedAddressId"
  }

  object MatchedOrderNetworkMessage {

    val messageId: Byte = 2: Byte
  }

  object MatchedOrderSerializer extends NetworkMessageProtoSerializer[MatchedOrderNetworkMessage] {

    override def toProto(message: MatchedOrderNetworkMessage): InnerMessage =
      MO(MatchedOrderProtoMessage()
        .withMatchedOrderId(message.orderId)
        .withPrice(message.price)
        .withVolume(message.volume)
        .withMatchedAddressId(message.matchedAddressId))

    override def fromProto(message: InnerMessage): Option[MatchedOrderNetworkMessage] = message.matchedOrder match {
      case Some(matchedOM) =>
        Some(MatchedOrderNetworkMessage(
          matchedOM.matchedOrderId,
          matchedOM.price,
          matchedOM.volume,
          matchedOM.matchedAddressId))
      case _ => Option.empty[MatchedOrderNetworkMessage]
    }
  }

  case class TradeDirectiveNetworkMessage(targetOrderOwnerClientId: String,
                                          counterPartyClientId: String,
                                          assetId: String,
                                          price: Long,
                                          volume: Long,
                                          order: Int) extends NetworkMessageProto {

    override val messageName: String = "Transfer Directive"
    override val messageId: Byte = MatchedOrderNetworkMessage.messageId

    override def toInnerMessage: InnerMessage = TradeDirectiveSerializer.toProto(this)

    override def toString: String = s"$messageName - orderId: price: $price, volume: $volume,"
  }

  object TradeDirectiveNetworkMessage {

    val messageId: Byte = 2: Byte
  }

  object TradeDirectiveSerializer extends NetworkMessageProtoSerializer[TradeDirectiveNetworkMessage] {

    override def toProto(message: TradeDirectiveNetworkMessage): InnerMessage =
      TD(TradeDirectiveProtoMessage()
        .withPrice(message.price)
        .withVolume(message.volume)
        .withTargetOrderOwnerClientId(message.targetOrderOwnerClientId)
        .withCounterPartyClientId(message.counterPartyClientId)
        .withAssetId(message.assetId)
          .withOrder(message.order)
      )

    override def fromProto(message: InnerMessage): Option[TradeDirectiveNetworkMessage] = message.tradeDirective match {
      case Some(matchedOM) =>
        Some(TradeDirectiveNetworkMessage(
          matchedOM.targetOrderOwnerClientId,
          matchedOM.counterPartyClientId,
          matchedOM.assetId,
          matchedOM.price,
          matchedOM.volume,
          matchedOM.order)
        )
      case _ => Option.empty[TradeDirectiveNetworkMessage]
    }
  }

  case object GeneralizedNetworkMessageProto {

    def toProto(message: NetworkMessageProto): GeneralizedNetworkMessage =
      GeneralizedNetworkMessage().withInnerMessage(message.toInnerMessage)

    def fromProto(message: ByteString): Try[NetworkMessageProto] = Try {
      val networkMessage: GeneralizedNetworkMessage = GeneralizedNetworkMessage.parseFrom(message.toArray)
      networkMessage.innerMessage match {
        case InnerMessage.OrderRequest(_) => OrderRequestSerializer.fromProto(networkMessage.innerMessage).get
        case InnerMessage.MatchedOrder(_) => MatchedOrderSerializer.fromProto(networkMessage.innerMessage).get
        case InnerMessage.TradeDirective(_) => TradeDirectiveSerializer.fromProto(networkMessage.innerMessage).get
      }
    }
  }

}