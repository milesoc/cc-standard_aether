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
//TODO Obviously lots of null checks and other error handling.
//This would probably be better used for simply getting the SMString/SMInt/etc out, since that will be null without
//throwing a nullpointer as readily. I can then Option() that and map it to its real type in scala.
public class Reader {
  public static String getString(String fieldName, SMObject rawObj) {
    return ((SMString)(rawObj.getValue().get(fieldName))).getValue();
  }

  public static long getLong(String fieldName, SMObject rawObj) {
    return ((SMInt)(rawObj.getValue().get(fieldName))).getValue();
  }

  public static SMObject getSubObject(String fieldName, SMObject rawObj) {
    return ((SMObject)(rawObj.getValue().get(fieldName)));
  }

}
