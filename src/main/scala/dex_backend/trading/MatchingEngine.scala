package dex_backend.trading

import dex_backend.trading.order.{AggregatedOrders, Order, OrderBook}

import scala.annotation.tailrec
import scala.collection.immutable.TreeSet

class MatchingEngine {

  def matchCycle(orderBook: OrderBook): List[TradeDirective] = {
    @tailrec
    def loop(buyLevels: List[AggregatedOrders],
             sellLevels: List[AggregatedOrders],
             outputDirectives: List[TradeDirective]): List[TradeDirective] =
      (buyLevels, sellLevels) match {
        case (buyLevel :: buyTail, sellLevel :: sellTail)
          if buyLevel.price >= sellLevel.price =>
          val buyPrice = buyLevel.price
          val sellPrice = sellLevel.price
          if (buyPrice == sellPrice) {
            val resultedDirectives = matchLevel(buyLevel.orders, sellLevel.orders, List.empty)
              .flatMap(_.toSellerMostDirectives)
            loop(buyTail, sellTail, outputDirectives ++ resultedDirectives)
          }
          else loop(buyTail, sellLevels, outputDirectives)
        case _ =>
          outputDirectives
      }
    loop(orderBook.aggregatedBuyOrders.toList, orderBook.aggregatedSellOrders.toList, List.empty)
  }

  @tailrec
  private def matchLevel(buyLevel: TreeSet[Order],
                         sellLevel: TreeSet[Order],
                         matchedAcc: List[MatchResult]): List[MatchResult] = {
    (buyLevel ++ sellLevel).toList.sortBy(_.timestamp) match {
      case head :: _ =>
        val (acquiredOrders, peOrderOpt, _) = (if (head.direction.isBuy) sellLevel else buyLevel)
          .foldLeft(List.empty[Order], None: Option[Order], 0L) {
            case ((acc, partiallyExecutedOrderOpt, accAmount), order) if accAmount < head.volume =>
              val volumeLeftToFill = head.volume - accAmount
              if (order.volume <= volumeLeftToFill) {
                (acc :+ order, partiallyExecutedOrderOpt, accAmount + order.volume)
              } else {
                assert(partiallyExecutedOrderOpt.isEmpty, "Illegal cycle state. `partiallyExecutedOrderOpt.isEmpty` check")
                val partiallyExecutedOrder = order.executePartially(volumeLeftToFill)
                (acc :+ order, Some(partiallyExecutedOrder), accAmount + volumeLeftToFill)
              }
            case ((acc, partiallyExecutedOrderOpt, accAmount), _) =>
              assert(accAmount == head.volume, s"Illegal cycle state. `$accAmount == ${head.volume}` check")
              (acc, partiallyExecutedOrderOpt, accAmount)
          }
        val buyLevelUpdated: TreeSet[Order] =
          if (head.direction.isBuy) buyLevel - head
          else acquiredOrders.foldLeft(buyLevel)(_ - _) ++ peOrderOpt.toList
        val sellLevelUpdated: TreeSet[Order] =
          if (head.direction.isSell) sellLevel - head
          else acquiredOrders.foldLeft(sellLevel)(_ - _) ++ peOrderOpt.toList
        val result = MatchResult(head, acquiredOrders, peOrderOpt)
        matchLevel(buyLevelUpdated, sellLevelUpdated, matchedAcc :+ result)
      case Nil =>
        matchedAcc
    }
  }

}
