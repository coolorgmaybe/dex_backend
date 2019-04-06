package dex_backend.trading

final case class TradeDirective(targetOrderOwnerClientId: String,
                                counterPartyClientId: String,
                                assetId: String,
                                volume: Long,
                                price: Long)
