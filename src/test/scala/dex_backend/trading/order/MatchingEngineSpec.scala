package dex_backend.trading.order

import dex_backend.generators.DexGenerators
import dex_backend.trading.MatchingEngine
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.scalatest.{FlatSpec, Matchers}

import scala.util.Try

class MatchingEngineSpec extends FlatSpec with Matchers with GeneratorDrivenPropertyChecks {

  private val engine = new MatchingEngine

  it should "match orders" in {
    forAll(DexGenerators.orderBookGen) { orderBook =>
      Try(engine.matchCycle(orderBook)) shouldBe 'success
      println(engine.matchCycle(orderBook))
    }
  }

}
