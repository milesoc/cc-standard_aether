package com.milesoc.sa.models

import com.stackmob.sdkapi._
import com.milesoc.sa.core.Reader
import scala.collection.JavaConverters._

/**
 * Created by IntelliJ IDEA.
 * User: miles
 * Date: 2/18/13
 */
case class PlayerCharacter(id: String,
                     name: String,
                     money: Long,
                     markets: Map[Market, Long]) {

  def invested: Long = markets.foldLeft(0L){(inv, entry) => {
    val (market, quantity) = entry
    inv + (market.price*quantity)
  }}

}

object PlayerCharacter {

  def getAllCharacters(provider: SDKServiceProvider) = {
    val ds = provider.getDataService
    //read all markets from the db
    val filters = new ResultFilters(0,
      -1,
      List[SMOrdering](new SMOrdering("name", OrderingDirection.ASCENDING)).asJava,
      List("character_id", "name", "money").asJava)

    val charactersRaw = ds.readObjects("character", List[SMCondition]().asJava, 1, filters).asScala
    val markets = charactersRaw.map (characterRaw => {
      parseCharacter(characterRaw, provider)
    })
    markets.toList.filter(_.isDefined).map(_.get)
  }


  def getCharacter(id: String, provider: SDKServiceProvider): PlayerCharacter = {
    val ds = provider.getDataService
    val conditions = List[SMCondition](new SMEquals("character_id", new SMString(id))).asJava
    //read all markets from the db
    val charactersRaw = ds.readObjects("character", conditions).asScala
    val characters = charactersRaw.map (characterRaw => {
      parseCharacter(characterRaw, provider)
    })
    characters.toList.filter(_.isDefined).map(_.get).head
  }


  def parseCharacter(characterRaw: SMObject, provider: SDKServiceProvider): Option[PlayerCharacter] = {
    val logger: LoggerService = provider.getLoggerService(getClass)
    val characterProcessed = (for {
      id <- Option(Reader.getString("character_id", characterRaw))
      name <- Option(Reader.getString("name", characterRaw))
      _ <- Some(logger.debug("got character %s".format(name)))
      money <- Option(Reader.getLong("money", characterRaw))
      markets <- Option(getMarketsForChar(id, provider))
      character <- Some(new PlayerCharacter(id, name, money, markets))
    } yield character)
    if (characterProcessed.isEmpty)
      logger.error("Failed to parse character: %s".format(Option(Reader.getString("character_id", characterRaw))
    characterProcessed
  }

  def getMarketsForChar(id: String, provider: SDKServiceProvider): Map[Market, Long] = {
    //get all active_market entries with this char's name
    val ds = provider.getDataService
    val activeRaw = ds.readObjects("active_market", List[SMCondition](new SMEquals("character", new SMString(id))).asJava, 2)
    val actives = activeRaw.asScala.map (activeMarket => {
      for {
        market <- Market.parseMarket(Reader.getSubObject("market", activeMarket), provider)
        quantity <- Option(Reader.getLong("quantity", activeMarket))
      } yield (market, quantity)
    })
    actives.toList.filter(_.isDefined).map(_.get).toMap
  }

}
