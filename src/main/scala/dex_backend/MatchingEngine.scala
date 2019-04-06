package dex_backend

import dex_backend.order.{Order, OrderBook}

class MatchingEngine {

  def matchCycle(orderBook: OrderBook): Option[(List[Order], OrderBook)] = ???

}
