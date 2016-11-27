package com.hamlazot.common.serialization

import com.hamlazot.common.macros.Macros.Mappable

/**
 * @author yoav @since 11/27/16.
 */
trait Mapper {
  def materialize[T: Mappable](map: Map[String, Any]) =
    implicitly[Mappable[T]].fromMap(map)

}
