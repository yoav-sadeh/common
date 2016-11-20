package com.hamlazot.common.tests.specs

import java.time.ZoneOffset

import com.hamlazot.common.serialization.{CamelcaseDeseiralizationTransformer, JsonSerializer, SnakecaseSerializationTransformer}
import com.hamlazot.common.tests.mocks.MockEnum.MockEnum
import com.hamlazot.common.tests.mocks.{CaseClassWithInt, MockCaseClass, MockDateTimeContainer, MockEnum}
import org.specs2.mutable.Specification

/**
 * @author yoav @since 2/20/16.
 */
class JsonSerializerSpec extends Specification with JsonSerializer with SnakecaseSerializationTransformer with CamelcaseDeseiralizationTransformer {
  {
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

    }
  }
}

