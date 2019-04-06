package dex_backend.order

sealed trait OrderDirection {
  val isSell: Boolean
  val isBuy: Boolean = !isSell
}

object OrderDirection {
  case object Sell extends OrderDirection { override val isSell: Boolean = true }
  case object Buy extends OrderDirection { override val isSell: Boolean = false }
}
