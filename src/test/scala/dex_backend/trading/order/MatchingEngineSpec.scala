package dex_backend.trading.order

import dex_backend.generators.DexGenerators
import dex_backend.trading.{BestExecutionMatchingEngine, StrictMatchingEngine}
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.scalatest.{FlatSpec, Matchers}

import scala.collection.immutable.TreeSet
import scala.util.Try

class MatchingEngineSpec extends FlatSpec with Matchers with GeneratorDrivenPropertyChecks {

  private val strictEngine = new StrictMatchingEngine
  private val bestExecEngine = new BestExecutionMatchingEngine

  val orderBook = OrderBook(
    "ETT",
    "BTC",
    TreeSet(Order("ETT", "BTC", "client_1", OrderDirection.Sell, 1000, 10, 1123499))(OrderBook.sellOrd),
    TreeSet(Order("ETT", "BTC", "client_2", OrderDirection.Buy, 1100, 10, 112345))(OrderBook.buyOrd)
  )

  it should "match orders strictly" in {
    forAll(DexGenerators.orderBookGen) { orderBook =>
      Try(strictEngine.doMatch(orderBook)) shouldBe 'success
    }
  }

  it should "match orders by best execution" in {
    forAll(DexGenerators.orderBookGen) { orderBook =>
      Try(bestExecEngine.doMatch(orderBook)) shouldBe 'success
    }
  }

}
