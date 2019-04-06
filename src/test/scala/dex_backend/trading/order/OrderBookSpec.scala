package dex_backend.trading.order

import dex_backend.generators
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.scalatest.{FlatSpec, Matchers}

class OrderBookSpec extends FlatSpec with Matchers with GeneratorDrivenPropertyChecks {

  it should "keep orders sorted" in {
    forAll(generators.DexGenerators.orderBookGen) { orderBook =>
      println(orderBook.aggregatedBuyOrders)
      println(orderBook.aggregatedSellOrders)
    }
  }

}
