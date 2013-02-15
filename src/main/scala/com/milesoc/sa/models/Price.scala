package com.milesoc.sa.models

import com.stackmob.sdkapi._
import scala.collection.JavaConverters._
import com.milesoc.sa.core.Reader

/**
 * Created by IntelliJ IDEA.
 * User: miles
 * Date: 2/14/13
 */
object Price {

  def getPrice(marketId: String, provider: SDKServiceProvider): Option[Long] = {
    val ds = provider.getDataService
    val conditions = List[SMCondition](new SMEquals("market", new SMString(marketId))).asJava
    val resultFilters = new ResultFilters(0,
      1,
      List[SMOrdering](new SMOrdering("createddate", OrderingDirection.DESCENDING)).asJava,
      List[String]("price").asJava)
    val latestPrice = ds.readObjects("price", conditions, 1, resultFilters)
    latestPrice.asScala.headOption.map(priceObj => {
      Reader.convertPrice(priceObj)
    })
  }

}
