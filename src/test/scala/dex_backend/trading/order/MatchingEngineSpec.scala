package dex_backend.trading.order

import dex_backend.generators.DexGenerators
import dex_backend.trading.MatchingEngine
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.scalatest.{FlatSpec, Matchers}

import scala.collection.immutable.TreeSet
import scala.util.Try

class MatchingEngineSpec extends FlatSpec with Matchers with GeneratorDrivenPropertyChecks {

  private val engine = new MatchingEngine

  val orderBook = OrderBook(
    "ETT",
    "BTC",
    TreeSet(Order("ETT", "BTC", "client_1", OrderDirection.Sell, 1000, 10, 11234))(OrderBook.sellOrd),
    TreeSet(Order("ETT", "BTC", "client_2", OrderDirection.Buy, 1000, 10, 112345))(OrderBook.sellOrd)
  )

  it should "match orders" in {
    forAll(DexGenerators.orderBookGen) { orderBook =>
      Try(engine.matchCycle(orderBook)) shouldBe 'success
    }
  }

}
