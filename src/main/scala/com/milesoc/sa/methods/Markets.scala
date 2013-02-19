/**
 * Copyright 2013 Miles O'C
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.milesoc.sa.methods

import com.stackmob.core.customcode.CustomCodeMethod
import com.stackmob.sdkapi._
import com.stackmob.core.rest.{ResponseToProcess, ProcessedAPIRequest}
import java.util.{List => JList, Map => JMap}
import scala.collection.JavaConverters._
import com.milesoc.sa.core.Utils._
import java.{lang, util}
import com.milesoc.sa.models.Market
import com.stackmob.core.MethodVerb
import lang.Double

class Markets extends CustomCodeMethod {

  override def getMethodName: String = "markets"

  override def getParams: JList[String] = List[String]().asJava

  override def execute(request: ProcessedAPIRequest, serviceProvider: SDKServiceProvider): ResponseToProcess = {
    try {
      request.getLoggedInUser match {
        case null => errorResponse(401, "Not logged in")
        case user => request.getVerb match {
          //for now user won't matter since it shows all markets, etc, but this is a good pattern
          case MethodVerb.GET => getMarkets(request, serviceProvider, user)
          case _ => errorResponse(400, "Only GET is allowed at the moment")
        }
      }
    } catch {
      case t => {
        val logger = serviceProvider.getLoggerService(getClass)
        logger.error("Failed to execute characters", t)
        errorResponse(500, "internal error")
      }
    }
  }

  def getMarkets(request: ProcessedAPIRequest, provider: SDKServiceProvider, user: String): ResponseToProcess = {
    val markets = Market.getAllMarkets(provider)
    val resultMap = new util.HashMap[String, Object]()
    val resultList = new util.ArrayList[JMap[String, Object]]()
    markets foreach(market => {
      val marketMap = new util.LinkedHashMap[String, Object]()
      marketMap.put("id", market.id)
      marketMap.put("commodity", market.commodity)
      marketMap.put("price", new lang.Long(market.price))
      marketMap.put("trend", new Double(0.0))
      marketMap.put("quantity", new lang.Long(market.qty))
      marketMap.put("investment", new lang.Long(market.investment))
      resultList.add(marketMap)
    })

    resultMap.put("result", resultList)
    new ResponseToProcess(200, resultMap)
  }



}
