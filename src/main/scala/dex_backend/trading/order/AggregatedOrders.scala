package dex_backend.trading.order

import scala.collection.immutable.TreeSet

final case class AggregatedOrders(assetId: String,
                                  exchangeAssetId: String,
                                  price: Long,
                                  direction: OrderDirection,
                                  orders: TreeSet[Order]) {
  def remove(order: Order): AggregatedOrders = this.copy(orders = orders - order)
}
