package dex_backend.trading.order

import com.google.common.primitives.{Bytes, Longs}
import scorex.crypto.hash.Blake2b256
import scorex.util.encode.Base16

final case class Order(assetId: String,
                       clientId: String,
                       direction: OrderDirection,
                       price: Long,
                       volume: Long,
                       timestamp: Long,
                       status: OrderStatus = OrderStatus.Unknown,
                       partiallyExecuted: Boolean = false) {

  val id: String = Base16.encode(
    Bytes.concat(
      Blake2b256.hash(assetId + clientId),
      Longs.toByteArray(price),
      Longs.toByteArray(timestamp),
      if (direction.isBuy) Array(0x00) else Array(0x01)
    )
  )

  def setStatus(newStatus: OrderStatus): Order = this.copy(status = newStatus)
}
