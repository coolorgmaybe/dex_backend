package dex_backend.trading.order

import com.google.common.primitives.{Bytes, Ints, Longs}
import scorex.crypto.hash.Blake2b256

final case class Order(assetId: String,
                       exchangeAssetId: String,
                       clientId: String,
                       direction: OrderDirection,
                       price: Long,
                       volume: Long,
                       timestamp: Long,
                       status: OrderStatus = OrderStatus.Unknown,
                       partiallyExecuted: Boolean = false) {

  val id: Int = Ints.fromByteArray(
    Blake2b256
      .hash(
        Bytes.concat(
          Blake2b256.hash(assetId + exchangeAssetId + clientId),
          Longs.toByteArray(price),
          Longs.toByteArray(timestamp),
          if (direction.isBuy) Array(0x00) else Array(0x01)
        )
      )
      .take(4)
  )

  def setStatus(newStatus: OrderStatus): Order = this.copy(status = newStatus)

  def executePartially(executedVolume: Long): Order =
    this.copy(volume = volume - executedVolume, partiallyExecuted = true)

  override def hashCode(): Int = id

  override def equals(obj: Any): Boolean = obj match {
    case that: Order => that.id == id
    case _ => false
  }

}
