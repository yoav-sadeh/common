package com.hamlazot.common.serialization

import org.json4s.JValue

/**
 * @author yoav @since 12/7/16.
 */

trait SnakecaseTransformerUUIDSupport extends SerializationTransformer {
  override def transformSerialized(value: JValue): JValue = snakizeKeys(value)

  def snakizeKeys(value: JValue): JValue = {
    value.transformField {
      case (nm, x) => (underscore(nm), x)
    }
  }

  def underscore(word: String): String = {

    val firstPattern = "([A-Z]+)([A-Z][a-z])".r
    val secondPattern = "([a-z\\d])([A-Z])".r
    val replacementPattern = "$1_$2"

    secondPattern.replaceAllIn(
      firstPattern.replaceAllIn(
        word, replacementPattern), replacementPattern).toLowerCase
  }

}
