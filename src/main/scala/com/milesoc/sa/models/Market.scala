package com.milesoc.sa.models

import com.stackmob.sdkapi._
import scala.collection.JavaConverters._
import com.milesoc.sa.core.Reader

/**
 * Created by IntelliJ IDEA.
 * User: miles
 * Date: 2/10/13
 */
case class Market(id: String,
                  commodity: String,
                  price: Long,
                  trend: Double,
                  qty: Long) {

  def investment = price * qty

}

object Market {

  final val TREND_DISCOUNT = 0.8

  def getMarket(id: String, provider: SDKServiceProvider): Market = {
    val ds = provider.getDataService
    val conditions = List[SMCondition](new SMEquals("market_id", new SMString(id))).asJava
    //read all markets from the db
    val marketsRaw = ds.readObjects("market", conditions).asScala
    val markets = marketsRaw.map (marketRaw => {
      parseMarket(marketRaw, provider)
    })
    markets.toList.filter(_.isDefined).map(_.get).head
  }

  def getAllMarkets(provider: SDKServiceProvider): List[Market] = {
    val ds = provider.getDataService
    val logger = provider.getLoggerService(getClass)
    //read all markets from the db
    val filters = new ResultFilters(0,
      -1,
      List[SMOrdering](new SMOrdering("commodity", OrderingDirection.ASCENDING)).asJava,
      List("market_id", "commodity").asJava)

    val marketsRaw = ds.readObjects("market", List[SMCondition]().asJava, 1, filters).asScala
    val markets = marketsRaw.map (marketRaw => {
      parseMarket(marketRaw, provider)
    })
    markets.toList.filter(_.isDefined).map(_.get)
  }


  def parseMarket(marketRaw: SMObject, provider: SDKServiceProvider): Option[Market] = {
    val logger: LoggerService = provider.getLoggerService(getClass)
    val marketProcessed = (for {
      id <- Option(Reader.getString("market_id", marketRaw))
      name <- Option(Reader.getString("commodity", marketRaw))
      _ <- Some(logger.debug("got commodity %s".format(name)))
      price <- Option(Price.getPrice(id, provider))
      trend <- Some(0)
      quantity <- Option(getQuantity(id, provider))
      market <- Some(Market(id, name, price, trend, quantity))
    } yield market)
    if (marketProcessed.isEmpty)
      logger.error("Failed to parse market: %s".format(Option(Reader.getString("commodity", marketRaw))))
    marketProcessed
  }

  def getQuantity(id: String, provider: SDKServiceProvider): Long = {
    val ds = provider.getDataService
    //get all involvements in this market
    val marketQuantities = ds.readObjects("active_market", List[SMCondition](new SMEquals("market", new SMString(id))).asJava)
    val allQuantities = marketQuantities.asScala.map(Reader.getLong("quantity", _)).toList
    allQuantities.foldLeft(0L){(a, b) => {
      a + b
    }}
  }

}
