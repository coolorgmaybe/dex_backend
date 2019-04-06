package dex_backend.trading

import dex_backend.trading.order.{Order, OrderDirection}

final case class MatchResult(orderToMeet: Order,
                             correspondingOrders: List[Order],
                             partiallyExecutedOrderOpt: Option[Order]) {

  def toSellerMostDirectives: List[TradeDirective] = orderToMeet.direction match {
    case OrderDirection.Sell => correspondingOrders.map { order =>
      TradeDirective(orderToMeet.clientId, order.clientId, orderToMeet.assetId, orderToMeet.volume, orderToMeet.price)
    }
    case OrderDirection.Buy => correspondingOrders.map { order =>
      TradeDirective(order.clientId, orderToMeet.clientId, order.assetId, order.volume, order.price)
    }
  }

}
