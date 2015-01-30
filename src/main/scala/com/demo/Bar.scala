package com.demo


import net.liftweb.json._
import net.liftweb._
import net.liftweb.common._
import net.liftweb.mongodb.record._
import net.liftweb.mongodb.record.field._
import net.liftweb.record.field._
import org.bson.types._
import com.foursquare.index._
import com.foursquare.rogue.LiftRogue._

/**
 * Created by doncorsean on 1/29/15.
 */
class Bar extends MongoRecord[Bar] with ObjectIdPk[Bar] with IndexedRecord[Bar] with Loggable  {
  def meta = Bar
}

object Bar extends Bar with MongoMetaRecord[Bar] with Loggable {
  def add(bar: Bar): Bar = {
    //foo.save
    bar
  }
}
