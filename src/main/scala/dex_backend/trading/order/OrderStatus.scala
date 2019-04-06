package dex_backend.trading.order

sealed trait OrderStatus

object OrderStatus {
  case object Unknown extends OrderStatus
  case object Available extends OrderStatus
  case object NotAvailable extends OrderStatus
  case object Removed extends OrderStatus
  case object MakerOffline extends OrderStatus
}
