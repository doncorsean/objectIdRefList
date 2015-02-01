package com.demo


import net.liftweb.json.JValue
import net.liftweb.json.JsonAST.{JNull, JNothing, JField, JValue}
import net.liftweb.json._
import net.liftweb._
import net.liftweb.common._
import net.liftweb.mongodb.ObjectIdSerializer
import net.liftweb.mongodb.record._
import net.liftweb.mongodb.record.field._
import net.liftweb.record.FieldHelpers
import net.liftweb.record.FieldHelpers._
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

  object idRef extends ObjectIdField(this)
  object idRefList1 extends ObjectIdRefListField(this, Bar) {
    override def setFromJValue(jvalue: JValue) = jvalue match {
      case JNothing|JNull if optional_? => setBox(Empty)
      case JArray(arr) =>
        println("set from is arr: "+arr)
        setBox(Full(arr.map {
          case JObject(JField("$oid", JString(s)) :: Nil) if (ObjectId.isValid(s)) => new ObjectId(s)
          case _ => throw new IllegalArgumentException()
        }))
      case other =>
        println("set from other")
        setBox(FieldHelpers.expectedA("JArray", other))
    }

  }
  object idRefList2 extends ObjectIdRefListField(this, Bar) {

    override def setFromJValue(jvalue: JValue) = jvalue match {
      case JNothing|JNull if optional_? => setBox(Empty)
      case JArray(arr) =>
        println("set from is arr: "+arr)
        setBox(Full(arr.map {
          case JObject(JField("$oid", JString(s)) :: Nil) if (ObjectId.isValid(s)) => new ObjectId(s)
          case _ => throw new IllegalArgumentException()
        }))
      case other =>
        println("set from other")
        setBox(FieldHelpers.expectedA("JArray", other))
    }

  }

}

object Foo extends Foo with MongoMetaRecord[Foo] with Loggable {
  import mongodb.BsonDSL._

  override def formats = DefaultFormats + new ObjectIdSerializer
  override def collectionName = "foos"

  /** Create a record by decoding a JValue which must be a JObject */
  override def fromJValue(jvalue: JValue): Box[Foo] = {
    val inst = createRecord
    setFieldsFromJValue(inst, jvalue) map (_ => inst)
  }

  /** Attempt to decode a JValue, which must be a JObject, into a record instance */
  override def setFieldsFromJValue(rec: Foo, jvalue: JValue): Box[Unit] = {
    def fromJFields(jfields: List[JField]): Box[Unit] = {
      for {
        jfield <- jfields
        field <- rec.fieldByName(jfield.name)
      } {
        println(s"Set field ${field.name}(${field.getClass.getName}}): "+pretty(render(jfield.value)))
        field.setFromJValue(jfield.value)
      }

      Full(())
    }

    jvalue match {
      case JObject(jfields) =>
        println("jobject")
        fromJFields(jfields)
      case other =>
        println("other")
        expectedA("JObject", other)
    }
  }

  def apply(in: JValue): Box[Foo] = {

    // Logs JSON passed to RestHelper
    logger.info(s"json before transform:${pretty(render(in))}")

    // Transforms JSON passed to RestHelper
//    val newJson = in transform {
//      case JField("idRefList1", JArray(idList)) => {
//        val objectIds = idList.collect {
//          case JString(id) => JObject(List(JField("$oid", id)))
//        }
//        JField("idRefList1", JArray(objectIds))
//      }
//      case JField("idRefList2", JArray(objectIdStrings)) => {
//        val objectIds = objectIdStrings.collect {
//          case JString(id) => JObject(List(JField("$oid", id)))
//        }
//        JField("idRefList2", JArray(objectIds))
//      }
//    }

    for {
      foo1 <- Full(Foo.createRecord.idRefList1(List(new ObjectId(),new ObjectId())).idRefList2(List(new ObjectId(),new ObjectId())))
      foo2 <- Foo.fromJValue(in)
//      foo3 <- Foo.fromJValue(newJson)
    } yield {
      foo1.idRefList1.get.map(id => logger.info(s"foo.idList1.get.map class:${id.getClass}"))

      println("FOO2 IS "+foo2.idRefList1)

      //Below causes java.lang.ClassCastException: java.lang.String cannot be cast to org.bson.types.ObjectId
      foo2.idRefList1.get.map(id => logger.info(s"foo2.idRefList1.get.map class:${id.isInstanceOf[ObjectId]}"))
      //foo3.idList1.get.map(id => logger.info(s"foo2.idList1.get.map class:${id.isInstanceOf[ObjectId]}"))

      logger.info(s"Foo.createRecord:${foo1}")
      logger.info(s"Deserialization of json from API (without transformation):${foo2}")
//      logger.info(s"Deserialization after transform:${foo3}")
    }

//    logger.info(s"json after transform:${pretty(render(newJson))}")

    // When ObjectIdRefListField is serialized Lift produces a List[String].
    // I was expecting fromJValue to realize that the field it was deserializing
    // was ObjectIdRefList and convert the List[String] to a List[ObjectId].
    // Instead it is storing into mongo as List[String] which causes
    // java.lang.ClassCastException: java.lang.String cannot be cast to org.bson.types.ObjectId
    // as well as other issues when searching and using Rogue to query as Rogue is expecting ObjectId.
    Foo.fromJValue(in)
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
      _.where((_.idRefList1 contains bar.id.get)),
      _.where((_.idRefList2 contains bar.id.get))
    ).fetch()
  }

}
