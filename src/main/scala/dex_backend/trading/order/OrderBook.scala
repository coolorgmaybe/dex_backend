package dex_backend.trading.order

import scala.collection.immutable.TreeSet

final case class OrderBook(assetId: String,
                           exchangeAssetId: String,
                           sellOrders: TreeSet[Order],
                           buyOrders: TreeSet[Order]) {
  import OrderBook._
  lazy val aggregatedSellOrders: TreeSet[AggregatedOrders] = {
    val aggregatedOrders: List[AggregatedOrders] = sellOrders
      .groupBy(_.price)
      .map { case (price, orders) =>
        AggregatedOrders(assetId, exchangeAssetId, price, OrderDirection.Sell, orders)
      }
      .toList
    TreeSet(aggregatedOrders:_*)(sellAggOrd)
  }
  lazy val aggregatedBuyOrders: TreeSet[AggregatedOrders] = {
    val aggregatedOrders: List[AggregatedOrders] = buyOrders
      .groupBy(_.price)
      .map { case (price, orders) =>
        AggregatedOrders(assetId, exchangeAssetId, price, OrderDirection.Buy, orders)
      }
      .toList
    TreeSet(aggregatedOrders:_*)(sellAggOrd)
  }
  def add(order: Order): OrderBook = order.direction match {
    case OrderDirection.Buy => this.copy(buyOrders = buyOrders + order)
    case OrderDirection.Sell => this.copy(sellOrders = sellOrders + order)
  }
  def remove(order: Order): OrderBook = order.direction match {
    case OrderDirection.Buy => this.copy(buyOrders = buyOrders - order)
    case OrderDirection.Sell => this.copy(sellOrders = sellOrders - order)
  }
}

object OrderBook {

  val sellOrd: Ordering[Order] = Ordering[(Long, Long)].on(x => (x.price, x.timestamp))
  val buyOrd: Ordering[Order] = Ordering[(Long, Long)].on(x => (-x.price, x.timestamp))

  val sellAggOrd: Ordering[AggregatedOrders] = Ordering[Long].on(_.price)
  val buyAggOrd: Ordering[AggregatedOrders] = Ordering[Long].on(x => -x.price)

  def empty(assetId: String, exchangeAssetId: String): OrderBook =
    OrderBook(assetId, exchangeAssetId, TreeSet.empty[Order](sellOrd), TreeSet.empty[Order](buyOrd))
}
