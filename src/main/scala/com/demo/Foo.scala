package com.demo


import net.liftweb.json._
import net.liftweb._
import net.liftweb.common._
import net.liftweb.mongodb.ObjectIdSerializer
import net.liftweb.mongodb.record._
import net.liftweb.mongodb.record.field._
import net.liftweb.record.field._
import org.bson.types._
import com.foursquare.index._
import com.foursquare.rogue.LiftRogue._

/**
 * Created with IntelliJ IDEA.
 * User: doncorsean
 * Date: 5/3/14
 * Time: 3:22 PM
 * To change this template use File | Settings | File Templates.
 */

class Foo extends MongoRecord[Foo] with ObjectIdPk[Foo] with IndexedRecord[Foo] with Loggable {

  def meta = Foo
  def idAsString: String = id.toString

  object date extends DateTimeField(this)
  object idList1 extends ObjectIdRefListField(this, Bar)
  object idList2 extends ObjectIdRefListField(this, Bar)

}

object Foo extends Foo with MongoMetaRecord[Foo] with Loggable {
  import mongodb.BsonDSL._

  override def formats = DefaultFormats + new ObjectIdSerializer
  override def collectionName = "foos"

  def apply(in: JValue): Box[Foo] = {
    logger.info(s"json in:${in}")
    val after = in transform {
      case JField("idList1", JArray(idList)) => {
        val objectIds = idList.collect {
          case JString(id) => JObject(List(JField("$oid", id)))
        }
        JField("idList1", JArray(objectIds))
      }
      case JField("idList2", JArray(objectIdStrings)) => {
        val objectIds = objectIdStrings.collect {
          case JString(id) => JObject(List(JField("$oid", id)))
        }
        JField("idList2", JArray(objectIds))
      }
    }
    logger.info(s"json after transform:${pretty(render(after))}")

    val res = Foo.fromJValue(after)
    logger.info(s"deserialization:${res}")
    res
  }
  def unapply(in: JValue): Option[Foo] = {
    apply(in)
  }
  def unapply(id: String): Option[Foo] = {
    Foo.find(id)
  }
  implicit def toJson(foo: Foo): JValue = {
    asJValue(foo)
  }
  implicit def toJson(foos: Seq[Foo]): JValue = {
    foos.map(aMatch => asJValue(aMatch))
  }
  def delete(id: ObjectId): Box[Foo] = synchronized {
    Foo.find(id).map{foo =>
      foo.delete_!
      foo
    }
  }
  def add(foo: Foo): Foo = {
    //foo.save
    foo
  }
  def getFoosFor(bar:Bar) = {
    Foo.or(
      _.where((_.idList1 contains bar.id.get)),
      _.where((_.idList2 contains bar.id.get))
    ).fetch()
  }

}
