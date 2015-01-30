package com.demo

import net.liftweb._
import common._
import http._
import rest._
import util._
import Helpers._
import json._
import scala.xml._
import net.liftweb.http.JsonResponse
import net.liftweb.http.rest.RestHelper
import net.liftweb.http.{ InMemoryResponse, StreamingResponse }
import net.liftweb.http.S
import net.liftweb.http.FileParamHolder
import net.liftweb.json.JsonAST.JValue
import net.liftweb.json.JsonDSL._
import net.liftweb.common.{ Box, Full }
import net.liftweb.http.BadResponse
import net.liftweb.util.StringHelpers
import com.foursquare.rogue.LiftRogue._
package api {

  object RestAPI extends RestHelper with Loggable {

    serve {

      case "foos" :: Nil JsonGet _ =>
        List(Foo.createRecord): JValue

      case "foos" :: Foo(foo) :: Nil JsonGet _ =>
        foo.asJValue

      case "foos" :: Foo(foo) :: Nil JsonPost json -> _ =>
        Foo(mergeJson(foo, json)).map(Foo.add(_).asJValue)

      case "foos" :: Nil JsonPut Foo(foo) -> _ =>
        Foo.add(foo) : JValue

    }
  }
}