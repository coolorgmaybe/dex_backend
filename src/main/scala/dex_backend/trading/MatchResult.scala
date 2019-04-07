package dex_backend.trading

import dex_backend.trading.order.{Order, OrderDirection}

final case class MatchResult(orderToMeet: Order,
                             correspondingOrders: List[Order],
                             partiallyExecutedOrderOpt: Option[Order]) {

  def toSellerMostDirectives: List[TradeDirective] = orderToMeet.direction match {
    case OrderDirection.Sell => correspondingOrders.map { order =>
      val priceToExecute = math.max(orderToMeet.price, order.price)
      TradeDirective(orderToMeet.clientId, order.clientId, orderToMeet.assetId,
        orderToMeet.volume, priceToExecute, orderToMeet)
    }
    case OrderDirection.Buy => correspondingOrders.map { order =>
      val priceToExecute = math.min(orderToMeet.price, order.price)
      TradeDirective(order.clientId, orderToMeet.clientId, order.assetId,
        order.volume, priceToExecute, orderToMeet)
    }
  }

}
