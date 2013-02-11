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
                  price: Long,
                  history: List[Long]) {

  //price history should assume earlier dates come first
  def calculateTrend: Double = {
    var currentDiscount = Market.TREND_DISCOUNT
    var changesSum = 0.0
    var discountsSum = 0.0
    calculateChanges foreach(change => {
      changesSum += currentDiscount * change
      discountsSum += currentDiscount
      currentDiscount *= Market.TREND_DISCOUNT
    })
    changesSum/discountsSum
  }

  /**
   * Most recent price changes come first
   */
  def calculateChanges: List[Double] = calculateChanges(List[Double](), history)

  private def calculateChanges(changes: List[Double], prices: List[Long]): List[Double] = prices match {
    case (p1 :: (p2 :: tl)) => {
      val newPrice = p2.toDouble
      val oldPrice = p1.toDouble
      val change = (newPrice-oldPrice)/((oldPrice+newPrice)/2) //economic % change
      calculateChanges(change :: changes, p2 :: tl)
    }
    case _ => changes
  }
}

object Market {

  final val TREND_DISCOUNT = 0.8

  def getAllMarkets(request: ProcessedAPIRequest, provider: SDKServiceProvider): List[Market] = {
    val logger = provider.getLoggerService(getClass)

    val ds = provider.getDataService
    //read all markets from the db
    val marketsRaw = ds.readObjects("market", List[SMCondition]().asJava).asScala
    val markets = marketsRaw.map (marketRaw => {
      for {
        id <- Option(Reader.getString("market_id", marketRaw))
        name <- Option(Reader.getString("commodity", marketRaw))
        price <- Option(Reader.convertPrice(marketRaw))
        history <- convertPriceHistory(marketRaw, logger, name)
        market <- Some(new Market(id, name, price, history))
      } yield market
    })
    markets.toList.filter(_.isDefined).map(_.get)
  }

  private def convertPriceHistory(marketRaw: SMObject, logger: LoggerService, name: String): Option[List[Long]] = {
    Reader.convertPriceHistoryToSMList(marketRaw).getValue match {
        case li: JList[_] => Some(li.asScala.map(_ match {
          case p: SMInt => p.getValue.asInstanceOf[Long]
          case _ => logger.error("Price history contains non-integer value"); 0L
        }).toList)
      case _ => logger.error("History is not a list for %s".format(name)); None
    }
  }

}
