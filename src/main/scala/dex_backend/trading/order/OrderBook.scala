package dex_backend.trading.order

import scala.collection.immutable.TreeSet

final case class OrderBook(sellOrders: TreeSet[Order], buyOrders: TreeSet[Order]) {
  implicit val aggregatedOrd: Ordering[AggregatedOrders] = Ordering[Long].on(_.price)
  lazy val aggregatedSellOrders: TreeSet[AggregatedOrders] = {
    val aggregatedOrders: List[AggregatedOrders] = sellOrders.groupBy(x => (x.assetId, x.price))
      .map { case ((assetId, price), orders) => AggregatedOrders(assetId, price, OrderDirection.Sell, orders) }
      .toList
    TreeSet(aggregatedOrders:_*)
  }
  lazy val aggregatedBuyOrders: TreeSet[AggregatedOrders] = {
    val aggregatedOrders: List[AggregatedOrders] = buyOrders.groupBy(x => (x.assetId, x.price))
      .map { case ((assetId, price), orders) => AggregatedOrders(assetId, price, OrderDirection.Buy, orders) }
      .toList
    TreeSet(aggregatedOrders:_*)
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

  implicit val ord: Ordering[Order] = Ordering[(Long, Long)].on(x => (x.price, x.timestamp))

  def empty: OrderBook = OrderBook(TreeSet.empty[Order], TreeSet.empty[Order])
}
