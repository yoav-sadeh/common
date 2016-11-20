package com.hamlazot.common.serialization

import org.json4s.JsonAST.JValue

/**
 * @author yoav @since 2/20/16.
 */
trait DeserializationTransformer {
  def transformDeserialized(value: JValue): JValue = value
}
