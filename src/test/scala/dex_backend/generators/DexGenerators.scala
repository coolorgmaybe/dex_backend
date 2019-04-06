package dex_backend.generators

import dex_backend.trading.order.{Order, OrderBook, OrderDirection, OrderStatus}
import org.scalacheck.Gen

import scala.collection.immutable.TreeSet

object DexGenerators {

  import OrderBook._

  def orderGen(direction: OrderDirection, assetId: String, exchangeAssetId: String): Gen[Order] = for {
    clientId <- Gen.oneOf(Seq("client_1", "client_2", "client_3"))
    price <- Gen.posNum[Long]
    volume <- Gen.posNum[Long]
    timestamp <- Gen.choose(123400000L, 1234567890L)
    status <- Gen.oneOf(Seq(OrderStatus.Available, OrderStatus.Unknown, OrderStatus.Removed, OrderStatus.MakerOffline))
  } yield Order(assetId, exchangeAssetId, clientId, direction, price, volume, timestamp, status)

  val orderBookGen: Gen[OrderBook] = for {
    assetId <- Gen.oneOf(Seq("ETT", "EFYT", "BTC"))
    exchangeAssetId <- Gen.oneOf(Seq("ERC", "CRD", "COK"))
    buyingOrders <- Gen.listOf(orderGen(OrderDirection.Buy, assetId, exchangeAssetId))
    sellingOrders <- Gen.listOf(orderGen(OrderDirection.Sell, assetId, exchangeAssetId))
  } yield OrderBook(assetId, exchangeAssetId, TreeSet(buyingOrders:_*)(sellOrd), TreeSet(sellingOrders:_*)(buyOrd))

}
