package com.hamlazot.common.tests.specs

import java.time.ZoneOffset
import java.util.UUID

import com.hamlazot.common.macros.Macros.Serializer
import com.hamlazot.common.serialization.{CamelcaseDeseiralizationTransformer, JsonSerializer, SnakecaseSerializationTransformer}
import com.hamlazot.common.tests.mocks.MockEnum.MockEnum
import com.hamlazot.common.tests.mocks.{CaseClassWithCustomMapKey, CaseClassWithInt, MockCaseClass, MockDateTimeContainer, MockEnum, NestingTrait}
import org.json4s.JObject
import org.specs2.mutable.Specification

/**
 * @author yoav @since 2/20/16.
 */
class JsonSerializerSpec
  extends Specification
  with JsonSerializer
  with SnakecaseSerializationTransformer
  with CamelcaseDeseiralizationTransformer
  with Marshaller {


  "JsonMarshaller " should {
    "deserialize enum" in {
      registerEnumForMarshalling(MockEnum)
      val request = MockCaseClass[MockEnum]("jojo", MockEnum.VALUE1)
      val serializedRequest = serialize(request)
      val des = deserialize[MockCaseClass[MockEnum.MockEnum]](serializedRequest)
      des shouldEqual request
    }

    "serialize object without char escapes" in {
      registerEnumForMarshalling(MockEnum)
      val request = MockCaseClass[MockEnum]("request", MockEnum.VALUE2)
      val serializedRequest = serialize(request)
      val deserializedRequest = deserialize[MockCaseClass[MockEnum.MockEnum]](serializedRequest)
      deserializedRequest shouldEqual request
    }

    "serialize ZonedDateTime" in {

      val container = MockDateTimeContainer()
      val serializedContainer = serialize(container)
      val deserializedContainer = deserialize[MockDateTimeContainer](serializedContainer)

      deserializedContainer.zdt shouldEqual container.zdt.withZoneSameInstant(ZoneOffset.UTC)
    }

    "serialize object with int" in {

      val container = CaseClassWithInt(3)
      val serializedContainer = serialize(container)
      val deserializedContainer = deserialize[CaseClassWithInt](serializedContainer)

      deserializedContainer.theInt shouldEqual container.theInt
    }

    "serialize sequence length" in {

      val seq = Seq(CaseClassWithInt(3), CaseClassWithInt(3), CaseClassWithInt(3))
      val container = CaseClassWithInt(seq.length)
      val serializedContainer = serialize(container)
      val deserializedContainer = deserialize[CaseClassWithInt](serializedContainer)

      deserializedContainer.theInt shouldEqual container.theInt
    }

    "serialize custom map keys" in {
      import java.util.UUID
      val json = """{
                     "name"  :"yoav",
                     "map": {
                       "4c6e254e-d9b0-4d1b-946a-845fb5ce8627": 5,
                       "4c6e254e-d9b0-4d1b-946a-845fb5ce8628": 3,
                       "4c6e254e-d9b0-4d1b-946a-845fb5ce8629": 1
                     }
                     }"""

      registerCustomKeySerializer[UUID](uuid => uuid.toString, str => UUID.fromString(str))

      val deserialized = deserialize[CaseClassWithCustomMapKey[UUID]](json)
      deserialized.map.keys.head shouldEqual UUID.fromString("4c6e254e-d9b0-4d1b-946a-845fb5ce8627")
    }

    "deserialize nested case classes" in {
      object nest extends NestingTrait
      import com.hamlazot.common.macros.Macros.Serializer
      val castable = implicitly[Serializer[nest.NestedCaseClass]]

      val nestedCaseClass = nest.NestedCaseClass("Jojo", 35)
      val serialized = serialize(nestedCaseClass)

      val nested = castable.deserializ(serialized)
      nested shouldEqual nestedCaseClass

    }

    "deserialize nested case classes in a generic manner" in {

      object nest extends NestingTrait
      val nestedCaseClass = nest.NestedCaseClass("Jojo", 35)
      val serialized = serialize(nestedCaseClass)

      val nested = marshal[nest.NestedCaseClass](serialized)
      nested shouldEqual nestedCaseClass
      true shouldEqual (true)
    }

    "deserialize nested case classes with abstract shit in it in a generic manner" in {
      object impl extends NestingTrait {
        type Trustees = String //Map[UUID, Int]
      }

      val nestedCaseClass = impl.NestedCaseClassWithShitInIt("Jojo", 35, UUID.randomUUID.toString)
      impl.NestedCaseClassWithShitInIt.unapply(nestedCaseClass)
      val serialized = serialize(impl.NestedCaseClassWithShitInIt.unapply(nestedCaseClass))
      val json = parse(serialized).asInstanceOf[JObject].children

      val nested = marshal[impl.NestedCaseClassWithShitInIt](serialized)
      nested shouldEqual nestedCaseClass
    }
  }

}

trait Marshaller {
  def marshal[A: Serializer](json: String): A = {
    val castable = implicitly[Serializer[A]]
    castable.deserializ(json)
  }
}