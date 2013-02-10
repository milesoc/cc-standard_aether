package com.milesoc.sa.core

import com.stackmob.core.rest.ResponseToProcess
import scala.collection.JavaConverters._

/**
 * Created by IntelliJ IDEA.
 * User: miles
 * Date: 2/10/13
 */
object Utils {

  def errorResponse(code: Int, msg: String) = new ResponseToProcess(code, Map("error" -> msg).asJava)

}
