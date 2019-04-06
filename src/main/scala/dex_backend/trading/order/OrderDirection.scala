package dex_backend.trading.order

sealed trait OrderDirection {
  val isSell: Boolean
  val isBuy: Boolean
}

object OrderDirection {
  case object Sell extends OrderDirection {
    override val isSell: Boolean = true
    override val isBuy: Boolean = false
  }
  case object Buy extends OrderDirection {
    override val isSell: Boolean = false
    override val isBuy: Boolean = true
  }
}
