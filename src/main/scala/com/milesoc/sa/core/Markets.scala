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

package com.milesoc.sa.core

import com.stackmob.core.customcode.CustomCodeMethod
import com.stackmob.sdkapi._
import com.stackmob.core.rest.{ResponseToProcess, ProcessedAPIRequest}
import java.util.{List => JList, Map => JMap}
import scala.collection.JavaConverters._
import com.milesoc.sa.core.Utils._
import java.util
import com.milesoc.sa.models.Market

class Markets extends CustomCodeMethod {

  override def getMethodName: String = "markets"

  override def getParams: JList[String] = List[String]().asJava

  override def execute(request: ProcessedAPIRequest, serviceProvider: SDKServiceProvider): ResponseToProcess = {
    request.getMethodName match {
      case "GET" => getMarkets(request, serviceProvider)
      case method => errorResponse(400, "Only GET is allowed at the moment, not %s".format(method))
    }
  }

  def getMarkets(request: ProcessedAPIRequest, provider: SDKServiceProvider): ResponseToProcess = {
    val markets = Market.getAllMarkets(request, provider)
    val resultMap = new util.HashMap[String, Object]()
    val resultList = new util.ArrayList[JMap[String, Object]]()
    markets foreach(market => {
      val marketMap = new util.LinkedHashMap[String, Object]()
      marketMap.put("commodity", market.commodity)
      marketMap.put("price", new java.lang.Long(market.price))
      marketMap.put("trend", new java.lang.Double(market.calculateTrend))
    })

    resultMap.put("markets", resultList)
    new ResponseToProcess(200, resultMap)
  }

}
