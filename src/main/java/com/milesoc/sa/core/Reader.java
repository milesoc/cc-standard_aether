/**
 * Created by IntelliJ IDEA.
 * User: miles
 * Date: 2/10/13
 */
package com.milesoc.sa.core;

import com.stackmob.sdkapi.SMInt;
import com.stackmob.sdkapi.SMList;
import com.stackmob.sdkapi.SMObject;
import com.stackmob.sdkapi.SMString;

//This is a terrible little class to handle some poor java-scala interactions. Hoping I can find a better way round this.
public class Reader {
  public static String convertCommodityName(SMObject marketRaw) {
    return ((SMString)(marketRaw.getValue().get("commodity"))).getValue();
  }

  public static Long convertPrice(SMObject marketRaw) {
    return ((SMInt)(marketRaw.getValue().get("price"))).getValue();
  }

  public static SMList<?> convertPriceHistoryToSMList(SMObject marketRaw) {
    return ((SMList<?>)(marketRaw.getValue().get("price_history")));
  }

}
