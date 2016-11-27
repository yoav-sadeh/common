package com.hamlazot.common.serialization

/**
 * @author yoav @since 11/27/16.
 */
sealed trait DeserializationStrategy
case object Naive extends DeserializationStrategy
case object Recurssive extends DeserializationStrategy

