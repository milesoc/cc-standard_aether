package com.milesoc.sa.models

import com.stackmob.core.rest.{ResponseToProcess, ProcessedAPIRequest}
import com.stackmob.sdkapi._
import scala.Some
import java.util.{List => JList}
import scala.List
import scala.Some
import scala.collection.JavaConverters._

/**
 * Created by IntelliJ IDEA.
 * User: miles
 * Date: 2/10/13
 */
class Market(val commodity: String,
             val price: Long,
             val history: List[Long]) {

  //price history should assume earlier dates come first
  def calculateTrend: Double = {
    var currentDiscount = Market.TREND_DISCOUNT
    var changesSum = 0
    var discountsSum = 0
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
  def calculateChanges = calculateChanges(List[Double](), history)

  private def calculateChanges(changes: List[Double], prices: List[Long]): List[Double] = prices match {
    case (p1 :: (p2 :: tl)) => {
      val change = (p2-p1)/((p1+p2)/2) //economic % change
      calculateChanges(change :: changes, p2 :: tl)
    }
    case _ => changes
  }
}

object Market {

  final val TREND_DISCOUNT = 0.5

  def getAllMarkets(request: ProcessedAPIRequest, provider: SDKServiceProvider): List[Market] = {
    val logger = provider.getLoggerService(getClass)

    val ds = provider.getDataService
    //read all markets from the db
    val marketsRaw = ds.readObjects("markets", List[SMCondition]().asJava).asScala
    val markets = marketsRaw.map (marketRaw => {
      for {
        name <- convertCommodityName(marketRaw)
        price <- convertPrice(marketRaw, logger, name)
        history <- convertPriceHistory(marketRaw, logger, name)
        market <- Some(new Market(name, price, history))
      } yield market
    })
    markets.toList.filter(_.isDefined).map(_.get)
  }

  private def convertPriceHistory(marketRaw: SMObject, logger: LoggerService, name: String): Option[List[Long]] = {
    Option(marketRaw.getValue.get("price_history")).flatMap(_ match {
      case hist: SMList[_] => hist.getValue match {
        case li: JList[_] => Some(li.asScala.map(_ match {
          case p: SMInt => p.getValue.asInstanceOf[Long]
          case _ => logger.error("Price history contains non-integer value"); 0L
        }).toList)
        case _ => logger.error("Price history is not a list"); None
      }
      case _ => logger.error("History is not a list for %s".format(name)); None
    })
  }

  private def convertCommodityName(marketRaw: SMObject): Option[String] = {
    Option(marketRaw.getValue.get("commodity")).map(_.getValue.toString)
  }

  private def convertPrice(marketRaw: SMObject, logger: LoggerService, name: String): Option[Long] = {
    Option(marketRaw.getValue.get("price")).flatMap(_ match {
      case p: SMInt => Some(p.getValue)
      case _ => logger.error("Invalid price found for %s".format(name)); None
    })
  }

}
