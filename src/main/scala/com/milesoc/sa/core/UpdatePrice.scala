package com.milesoc.sa.core

import com.stackmob.core.rest.{ResponseToProcess, ProcessedAPIRequest}
import com.stackmob.sdkapi.SDKServiceProvider
import com.milesoc.sa.models.Market
import java.{lang, util}
import com.stackmob.core.customcode.CustomCodeMethod
import util.{List => JList}
import scala.List
import com.stackmob.core.MethodVerb
import com.milesoc.sa.core.Utils._
import scala.collection.JavaConverters._

/**
 * Created by IntelliJ IDEA.
 * User: miles
 * Date: 2/10/13
 */
class UpdatePrice extends CustomCodeMethod {

  override def getMethodName: String = "update_price"

  override def getParams: JList[String] = List[String]("id", "price").asJava

  override def execute(request: ProcessedAPIRequest, serviceProvider: SDKServiceProvider): ResponseToProcess = {
    request.getVerb match {
      case MethodVerb.PUT => updatePrice(request, serviceProvider)
      case _ => errorResponse(400, "Only GET is allowed at the moment")
    }
  }

  def updatePrice(request: ProcessedAPIRequest, provider: SDKServiceProvider): ResponseToProcess = {
    val toUpdate = request.getParams.get("id")
    val newPrice = request.getParams.get("price")
    val newTrend = Market.updatePrice(toUpdate, newPrice, provider)

    val resultMap = new util.HashMap[String, Object]()
    resultMap.put("id", toUpdate)
    resultMap.put("price", newPrice)
    resultMap.put("trend", new lang.Double(newTrend))

    new ResponseToProcess(200, resultMap)
  }
}
