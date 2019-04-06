package dex_backend.generators

import dex_backend.order.{Order, OrderBook, OrderDirection, OrderStatus}
import org.scalacheck.Gen

import scala.collection.immutable.TreeSet

object DexGenerators {

  import OrderBook.ord

  def orderGen(direction: OrderDirection): Gen[Order] = for {
    assetId <- Gen.oneOf(Seq("ETT", "EFYT", "BTC"))
    clientId <- Gen.oneOf(Seq("client_1", "client_2", "client_3"))
    price <- Gen.choose(100L, 1000L)
    volume <- Gen.choose(10L, 100L)
    timestamp <- Gen.choose(123400000L, 1234567890L)
    status <- Gen.oneOf(Seq(OrderStatus.Available, OrderStatus.Unknown, OrderStatus.Removed, OrderStatus.MakerOffline))
  } yield Order(assetId, clientId, direction, price, volume, timestamp, status)

  val orderBookGen: Gen[OrderBook] = for {
    buyingOrders <- Gen.listOf(orderGen(OrderDirection.Buy))
    sellingOrders <- Gen.listOf(orderGen(OrderDirection.Sell))
  } yield OrderBook(TreeSet(buyingOrders:_*), TreeSet(sellingOrders:_*))

}
