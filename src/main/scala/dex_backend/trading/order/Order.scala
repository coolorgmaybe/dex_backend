package dex_backend.trading.order

final case class Order(assetId: String,
                       clientId: String,
                       direction: OrderDirection,
                       price: Long,
                       volume: Long,
                       timestamp: Long,
                       status: OrderStatus = OrderStatus.Unknown) {

  def setStatus(newStatus: OrderStatus): Order = this.copy(status = newStatus)
}
