package com.hamlazot.common.tests.specs

import java.time.ZoneOffset

import com.hamlazot.common.serialization.{Naive, DeserializationStrategy, CamelcaseDeseiralizationTransformer, JsonSerializer, Mapper, SnakecaseSerializationTransformer}
import com.hamlazot.common.tests.mocks.MockEnum.MockEnum
import com.hamlazot.common.tests.mocks.{CaseClassWithCustomMapKey, CaseClassWithInt, MockCaseClass, MockDateTimeContainer, MockEnum, NestingTrait}
import org.specs2.mutable.Specification

/**
 * @author yoav @since 2/20/16.
 */
class JsonSerializerSpec
  extends Specification
  with JsonSerializer
  with SnakecaseSerializationTransformer
  with CamelcaseDeseiralizationTransformer
  with NestingTrait
  {


  "JsonMarshaller " should {
    "deserialize enum" in {
      import com.hamlazot.common.macros.Macros.Mappable
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
      object NaiveSerializer extends JsonSerializer {
        override val deserializationStrategy: DeserializationStrategy = Naive
      }

      import com.hamlazot.common.macros.Macros.Mappable
      val nestedCaseClass = NestedCaseClass("Jojo", 35)
      val serialized = NaiveSerializer.serialize(nestedCaseClass)
      val nested = NaiveSerializer.deseriamap[NestedCaseClass](serialized)

      nested shouldEqual nestedCaseClass
    }
  }

}
