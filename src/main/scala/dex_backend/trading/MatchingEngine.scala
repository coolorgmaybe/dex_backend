package dex_backend.trading

import dex_backend.trading.order.OrderBook

trait MatchingEngine {
  def doMatch(orderBook: OrderBook): (List[TradeDirective], OrderBook)
}
