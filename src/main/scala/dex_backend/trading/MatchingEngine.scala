package dex_backend.trading

import dex_backend.trading.order.{Order, OrderBook}

class MatchingEngine {

  def matchCycle(orderBook: OrderBook): Option[(List[Order], OrderBook)] = ???

}
