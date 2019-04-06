package dex_backend.order

import scala.collection.immutable.TreeSet

final case class AggregatedOrders(assetId: String, price: Long, direction: OrderDirection, orders: TreeSet[Order])