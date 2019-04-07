package dex_backend.trading

import dex_backend.trading.order.{Order, OrderDirection}

final case class MatchResult(askOrder: Order,
                             correspondingOrders: List[Order],
                             partiallyExecutedOrderOpt: Option[Order]) {

  def toSellerMostDirectives: List[TradeDirective] = askOrder.direction match {
    case OrderDirection.Sell => correspondingOrders.map { order =>
      val priceToExecute = math.max(askOrder.price, order.price)
      TradeDirective(askOrder.clientId, order.clientId, askOrder.assetId,
        askOrder.volume, priceToExecute, askOrder.id)
    }
    case OrderDirection.Buy => correspondingOrders.map { order =>
      val priceToExecute = math.min(askOrder.price, order.price)
      TradeDirective(order.clientId, askOrder.clientId, order.assetId,
        order.volume, priceToExecute, askOrder.id)
    }
  }

}
