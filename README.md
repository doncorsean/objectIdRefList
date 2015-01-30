# objectIdRefList
Demo of deserialization issue

#### To start:

~;container:start;container:reload /

##### Description of problem:

###### Serialization:
When ObjectIdRefListField is serialized Lift produces a List[String]. This is perfect and an expected serialization of ObjectIdRefListField. 

###### De-serialization:
I was expecting fromJValue in MetaRecord to recognize that the field it was deserializing was ObjectIdRefList and convert the serialized List[String] to a List[ObjectId] without manipulation of the JSON. Instead it is storing into mongo as List[String] which causes java.lang.ClassCastException: java.lang.String cannot be cast to org.bson.types.ObjectId as well search failure using Rogue to query as Rogue is expecting ObjectId.

I tried transforming the JSON to the format the ObjectIdSerializer expects ("$oid", id) but it is then stored in Mongo as "Map($oid -> 5449c9e4d4c6a72dc51949ff)"

`val newJson = in transform {
      case JField("idRefList1", JArray(idList)) => {
        val objectIds = idList.collect {
          case JString(id) => JObject(List(JField("$oid", id)))
        }
        JField("idRefList1", JArray(objectIds))
      }
      case JField("idRefList2", JArray(objectIdStrings)) => {
        val objectIds = objectIdStrings.collect {
          case JString(id) => JObject(List(JField("$oid", id)))
        }
        JField("idRefList2", JArray(objectIds))
      }
    }`

###### Sample HTTP request

PUT /foos HTTP/1.1
Content-Type: application/json
Host: localhost:8080
{
	"idRefList1": ["5449c9e4d4c6a72dc51949ff"],
	"idRefList2": ["5449cadfd4c6df9b3348f691"],
	"idRef": "54485112d4c6164eb9412dd0"
}

###### Sample Curl request

curl -i -HAccept:application/json -HContent-type:application/json -d'{
"idRefList1": ["5449c9e4d4c6a72dc51949ff"],
"idRefList2": ["5449cadfd4c6df9b3348f691"],
"idRef": "54485112d4c6164eb9412dd0"
}' -XPUT http://localhost:8080/foos

###### Expectation:

It was my (incorrect) assumption that I would be able to put/post json in the serialized structure lift served via get and have lift deserialize correctly. If the serialization causes List[ObjectId] => List[String] shouldn't the default deserialization behaviour be to perform the reverse?