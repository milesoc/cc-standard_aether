package com.milesoc.sa.models

import com.stackmob.core.rest.{ResponseToProcess, ProcessedAPIRequest}
import com.stackmob.sdkapi._
import scala.Some
import java.util.{List => JList}
import scala.List
import scala.Some
import scala.collection.JavaConverters._
import com.milesoc.sa.core.Reader

/**
 * Created by IntelliJ IDEA.
 * User: miles
 * Date: 2/10/13
 */
case class Market(id: String,
                  commodity: String,
                  price: Long) {

//  //price history should assume earlier dates come first
//  def calculateTrend: Double = {
//    if (calculateChanges.length == 0) return 0.0
//    var currentDiscount = Market.TREND_DISCOUNT
//    var changesSum = 0.0
//    var discountsSum = 0.0
//    calculateChanges foreach(change => {
//      changesSum += currentDiscount * change
//      discountsSum += currentDiscount
//      currentDiscount *= Market.TREND_DISCOUNT
//    })
//    changesSum/discountsSum
//  }
//
//  /**
//   * Most recent price changes come first
//   */
//  def calculateChanges: List[Double] = calculateChanges(List[Double](), history)
//
//  private def calculateChanges(changes: List[Double], prices: List[Long]): List[Double] = prices match {
//    case (p1 :: (p2 :: tl)) => {
//      val newPrice = p2.toDouble
//      val oldPrice = p1.toDouble
//      val change = (newPrice-oldPrice)/((oldPrice+newPrice)/2) //economic % change
//      val filtered = if (change.isNaN) 0 else change
//      calculateChanges(filtered :: changes, p2 :: tl)
//    }
//    case _ => changes
//  }
}

object Market {

  final val TREND_DISCOUNT = 0.8

  def getMarket(id: String, provider: SDKServiceProvider): Market = {
    val ds = provider.getDataService
    //read all markets from the db
    val marketsRaw = ds.readObjects("market", List[SMCondition](new SMEquals("market_id", new SMString(id))).asJava).asScala
    val markets = marketsRaw.map (marketRaw => {
      for {
        id <- Option(Reader.getString("market_id", marketRaw))
        name <- Option(Reader.getString("commodity", marketRaw))
        price <- Option(Reader.convertPrice(marketRaw))
        market <- Some(new Market(id, name, price))
      } yield market
    })
    markets.toList.filter(_.isDefined).map(_.get).head
  }


  def getAllMarkets(provider: SDKServiceProvider): List[Market] = {
    val ds = provider.getDataService
    val logger = provider.getLoggerService(getClass)
    //read all markets from the db
    val marketsRaw = ds.readObjects("market", List[SMCondition]().asJava).asScala
    val markets = marketsRaw.map (marketRaw => {
      val marketProcessed = (for {
        id <- Option(Reader.getString("market_id", marketRaw))
        name <- Option(Reader.getString("commodity", marketRaw))
        _ <- Some(logger.debug("got commodity %s".format(name)))
        price <- Option(Price.getPrice(id, provider))
        trend <- Some(0)
        market <- Some(new Market(id, name, price))
      } yield market)
      if (marketProcessed.isEmpty)
        logger.error("Failed to parse market: %s".format(marketRaw.getValue.asScala.toString))
      marketProcessed
    })
    markets.toList.filter(_.isDefined).map(_.get)
  }

}
