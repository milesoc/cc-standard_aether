/**
 * Created by IntelliJ IDEA.
 * User: miles
 * Date: 2/10/13
 */
package com.milesoc.sa.core;

import com.stackmob.sdkapi.SMInt;
import com.stackmob.sdkapi.SMObject;
import com.stackmob.sdkapi.SMString;

//This is a terrible little class to handle some poor java-scala interactions. Hoping I can find a better way round this.
public class Reader {
  public static String getString(String fieldName, SMObject marketRaw) {
    return ((SMString)(marketRaw.getValue().get(fieldName))).getValue();
  }

  public static Long convertPrice(SMObject rawObj) {
    return ((SMInt)(rawObj.getValue().get("price"))).getValue();
  }

}
