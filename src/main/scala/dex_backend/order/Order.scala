package dex_backend.order

// todo: serialization
final case class Order(assetId: String,
                       clientId: String,
                       direction: OrderDirection,
                       price: Long,
                       volume: Long,
                       timestamp: Long,
                       status: OrderStatus = OrderStatus.Unknown) {

  def setStatus(newStatus: OrderStatus): Order = this.copy(status = newStatus)
}
