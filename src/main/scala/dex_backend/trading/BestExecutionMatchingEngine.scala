package dex_backend.trading
import dex_backend.trading.order.{Order, OrderBook, OrderDirection}

import scala.annotation.tailrec
import scala.collection.immutable.TreeSet

class BestExecutionMatchingEngine extends MatchingEngine {

  override def doMatch(orderBook: OrderBook): (List[TradeDirective], OrderBook) = {
    val matchCandidate = (orderBook.sellOrders.headOption ++ orderBook.buyOrders.headOption).minBy(_.timestamp)
    val ordersToMatch = if (matchCandidate.direction.isBuy) orderBook.sellOrders else orderBook.buyOrders
    val (acquiredOrders, peOrderOpt) = acquireOrders(matchCandidate, ordersToMatch)
    val updatedOrderBook = if (acquiredOrders.nonEmpty) {
      val bookWithoutExecuted = acquiredOrders
        .foldLeft(orderBook)(_ remove _)
        .remove(matchCandidate)
      peOrderOpt.map(bookWithoutExecuted.add).getOrElse(bookWithoutExecuted)
    } else {
      orderBook
    }
    MatchResult(matchCandidate, acquiredOrders, peOrderOpt).toSellerMostDirectives -> updatedOrderBook
  }

  private def acquireOrders(targetOrder: Order, orders: TreeSet[Order]): (List[Order], Option[Order]) = {
    @tailrec
    def loop(acc: List[Order], rem: List[Order],
             peOrderOpt: Option[Order], accVolume: Long): (List[Order], Option[Order]) = {
      rem match {
        case headOrder :: tail if accVolume < targetOrder.volume &&
          isAcceptablePrice(targetOrder, headOrder) && headOrder.clientId != targetOrder.clientId =>
          val volumeLeftToFill = targetOrder.volume - accVolume
          if (headOrder.volume <= volumeLeftToFill) {
            loop(acc :+ headOrder, tail, peOrderOpt, accVolume + headOrder.volume)
          } else {
            assert(peOrderOpt.isEmpty, "Illegal cycle state. `peOrderOpt.isEmpty` check")
            val (peToLeft, peToAcquire) = headOrder.split(atVolume = volumeLeftToFill)
            loop(acc :+ peToAcquire, tail, Some(peToLeft), accVolume + peToAcquire.volume)
          }
        case _ :: tail =>
          loop(acc, tail, peOrderOpt, accVolume)
        case _ =>
          acc -> peOrderOpt
      }
    }
    loop(List.empty, orders.toList, None, 0L)
  }

  private def isAcceptablePrice(forOrder: Order, ofOrder: Order): Boolean =
    (forOrder.direction, ofOrder.direction) match {
      case (OrderDirection.Sell, OrderDirection.Buy) => forOrder.price <= ofOrder.price
      case (OrderDirection.Buy, OrderDirection.Sell) => forOrder.price >= ofOrder.price
      case _ => throw new Exception("Illegal match")
    }

}
