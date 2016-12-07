package com.hamlazot.common.serialization

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

import org.json4s.JsonAST.{JNull, JString}
import org.json4s.ext.{EnumNameSerializer, JavaTypesSerializers}
import org.json4s.jackson.JsonMethods
import org.json4s.jackson.Serialization.write
import org.json4s.{CustomKeySerializer, CustomSerializer, DefaultFormats}

import scala.collection.mutable

/**
 * @author yoav @since 2/20/16.
 */
trait JsonSerializer
  extends JsonMethods
  with SerializationTransformer
  with DeserializationTransformer {

  val deserializationStrategy: DeserializationStrategy = Recurssive
  private val enums: mutable.MutableList[EnumNameSerializer[_]] = mutable.MutableList.empty[EnumNameSerializer[_]]
  private val customKeySerializers: mutable.MutableList[CustomKeySerializer[_]] = mutable.MutableList.empty[CustomKeySerializer[_]]


  implicit def jsonFormat = (DefaultFormats ++ JavaTypesSerializers.all ++ enums.toSeq ++ Seq(ZonedDateTimeSerializer)).addKeySerializers(customKeySerializers.toList)

  def serialize(obj: AnyRef): String = {
    compact(render(parse(write(obj)).map(transformSerialized)))
  }

  def deserialize[A: Manifest](json: String): A = {
    val parsed = parse(json)

    parsed.map(transformDeserialized).extract[A]
  }

  def registerEnumForMarshalling[A <: Enumeration](enum: Enumeration) = enums += new EnumNameSerializer(enum)

  def registerCustomKeySerializer[A: Manifest](ser: A => String, des: String => A) = customKeySerializers += new CustomKeySerializer[A](format => ( {
    case s: String => des(s)
  }, {
    case k: A => ser(k)
  }))
}

case object ZonedDateTimeSerializer extends CustomSerializer[ZonedDateTime](format => ( {
  case JString(s) =>
    ZonedDateTime.parse(s)
  case JNull => null
}, {
  case zdt: ZonedDateTime =>
    JString(zdt.truncatedTo(ChronoUnit.SECONDS).format(DateTimeFormatter.ISO_INSTANT))
}
  ))
