package com.milesoc.sa.methods

import com.stackmob.core.customcode.CustomCodeMethod
import com.stackmob.sdkapi._
import com.stackmob.core.rest.{ResponseToProcess, ProcessedAPIRequest}
import java.util.{List => JList, Map => JMap}
import scala.collection.JavaConverters._
import com.milesoc.sa.core.Utils._
import java.{lang, util}
import com.milesoc.sa.models.PlayerCharacter
import com.stackmob.core.MethodVerb

/**
 * Created by IntelliJ IDEA.
 * User: miles
 * Date: 2/18/13
 */

class Characters extends CustomCodeMethod {

  override def getMethodName: String = "characters"

  override def getParams: JList[String] = List[String]().asJava

  override def execute(request: ProcessedAPIRequest, serviceProvider: SDKServiceProvider): ResponseToProcess = {
    try {
      request.getLoggedInUser match {
        case null => errorResponse(401, "Not logged in")
        case user => request.getVerb match {
          //for now user won't matter but in the future we can get only the user's characters
          case MethodVerb.GET => getCharacters(request, serviceProvider, user)
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

  def getCharacters(request: ProcessedAPIRequest, provider: SDKServiceProvider, user: String): ResponseToProcess = {
    val characters = PlayerCharacter.getAllCharacters(provider)
    val resultMap = new util.HashMap[String, Object]()
    val resultList = new util.ArrayList[JMap[String, Object]]()
    characters foreach(pc => {
      val characterMap = new util.LinkedHashMap[String, Object]()
      characterMap.put("id", pc.id)
      characterMap.put("name", pc.name)
      characterMap.put("money", new lang.Long(pc.money))
      characterMap.put("invested", new lang.Long(pc.invested))
      resultList.add(characterMap)
    })

    resultMap.put("result", resultList)
    new ResponseToProcess(200, resultMap)
  }



}
