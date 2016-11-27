package com.hamlazot.common.macros

import scala.language.experimental.macros
//import scala.reflect.macros.blackbox.Context
//import scala.reflect.macros.whitebox.Context

object Macros {
  // write macros here

  import scala.reflect.macros.whitebox.Context

  object Mappable {
    implicit def materializeMappable[T]: Mappable[T] =
    macro materializeMappableImpl[T]

    def materializeMappableImpl[T: c.WeakTypeTag](c: Context): c.Expr[Mappable[T]] = {
      import c.universe._
      val tpe = weakTypeOf[T]

      val declarations = tpe.decls
      val ctor = declarations.collectFirst {
        case m: MethodSymbol if m.isPrimaryConstructor => m
      }.get
      val fields = ctor.paramLists.head

      val toMapParams = fields.map { field =>
        val name = field.name.toTermName
        val mapKey: String = name.decodedName.toString
        q"$mapKey -> t.$name"
      }

      val companion = tpe.typeSymbol.companion
      def returnType(name: Name) = tpe.decl(name).typeSignature
      val fromMapParams = fields.map { field =>
        val name = field.name
        val decoded = name.decodedName.toString
        val returnType = tpe.decl(name).typeSignature
        q"map($decoded).asInstanceOf[$returnType]"
      }


      val result = c.Expr[Mappable[T]] { q"""
      new Mappable[$tpe] {
        def toMap(t: $tpe) = Map(..$toMapParams)
        def fromMap(map: Map[String, Any]) = $companion(..$fromMapParams)
      }
    """
      }

      //println(s"result: $result")
      result
    }

  }

  object TypePath{
    implicit def isInnerClass[T]: Boolean = macro isInnerClassImpl[T]

    def isInnerClassImpl[T: c.WeakTypeTag](c: Context): c.Expr[Boolean] = {
      import c.universe._
      val tpe = weakTypeOf[T]

      val inner = tpe.typeSymbol.info.termSymbol.isModule
      println(s"info: ${tpe.typeSymbol}")
      println(s"inner: $inner")
      c.Expr[Boolean]{q"""$inner"""}
    }
  }
  
  trait Mappable[T] {
    def toMap(t: T): Map[String, Any]
    def fromMap(map: Map[String, Any]): T
  }

}

