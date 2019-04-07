package dex_backend.trading

import dex_backend.trading.order.Order

final case class TradeDirective(targetOrderOwnerClientId: String,
                                counterPartyClientId: String,
                                assetId: String,
                                volume: Long,
                                price: Long,
                                targetOrder: Order)
