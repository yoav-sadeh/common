package com.hamlazot.common.macros

import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods

import scala.language.experimental.macros
import scala.reflect.ClassTag

//import scala.reflect.macros.blackbox.Context
//import scala.reflect.macros.whitebox.Context

object Macros {
  // write macros here

  import scala.reflect.macros.whitebox.Context

  object M {
    def ct[A: c.WeakTypeTag](c: Context): c.Expr[ClassTag[A]] = {
      import c.universe._
      val a = c.prefix.tree.tpe.member(TypeName("A")).typeSignature
      c.Expr(q"implicitly[ClassTag[$a]]").asInstanceOf[c.Expr[ClassTag[A]]]
    }
  }

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

  object Serializer {

    implicit def materializeCastable[T]: Serializer[T] =
    macro materializeCastableImpl[T]

    def materializeCastableImpl[T: c.WeakTypeTag](c: Context): c.Expr[Serializer[T]] = {
      import c.universe._
      val tpe = weakTypeOf[T]
      val pred = tpe.asInstanceOf[scala.reflect.internal.Types#ClassTypeRef]
      val infoList = pred.pre.typeSymbol.info.members.toList
      val declarations = tpe.decls
      val ctor = declarations.collectFirst {
        case m: MethodSymbol if m.isPrimaryConstructor => m
      }.get

      val fields = ctor.paramLists.flatten

      val toMapParams = fields.map { field =>
        val name = field.name.toTermName
        val mapKey: String = name.decodedName.toString
        q"t.$name"
      }


      val ctorParams = fields.map { field =>
        val name = field.asTerm.name
        val typeName = field.info.typeSymbol.asType.name
        val mapKey: String = name.decodedName.toString
        q"$name : $typeName"
      }

      try {
        val barbaraParams = fields.map { field =>
          val name = field.asTerm.name
          val typeName = field.info.typeSymbol.asType.name
          val mapKey: String = name.decodedName.toString
          val realTypeName = if (field.info.resultType.typeSymbol.isAbstract) {

            val info = infoList.find(m => m.nameString == field.info.resultType.typeSymbol.name.toString).get.info
            info.typeSymbol.name
          } else {
            typeName
          }
          TypeApply(Select(Apply(Ident(TermName("json")),
            List(Literal(Constant(fields.indexOf(field))))), TermName("extract")),
            List(Ident(TypeName(realTypeName.toString))))
        }


        val rawiZawi = Apply(Select(Ident(TermName(pred.pre.typeSymbol.name.toString)), TermName(tpe.typeSymbol.asClass.name.toString)),
            barbaraParams)

        println(s"rawi: $rawiZawi")


        val result = c.Expr[Serializer[T]] { q"""


      new Serializer[$tpe] {
        def deserializ(jsonStr: String): $tpe = {
        val json = parse(jsonStr).asInstanceOf[JObject].children

        val result = $rawiZawi
        result
      }
      }
    """
        }

        println(result)
        result

      } catch {
        case any: Throwable =>
          println(s"excepzione: $any")
          null
      }

    }
  }

  object TypePath {
    implicit def isInnerClass[T]: Boolean = macro isInnerClassImpl[T]

    def isInnerClassImpl[T: c.WeakTypeTag](c: Context): c.Expr[Boolean] = {
      import c.universe._
      val tpe = weakTypeOf[T]

      val inner = tpe.typeSymbol.info.termSymbol.isModule
      println(s"info: ${tpe.typeSymbol}")
      println(s"inner: $inner")
      c.Expr[Boolean] { q"""$inner"""}
    }
  }

  trait Mappable[T] {
    def toMap(t: T): Map[String, Any]

    def fromMap(map: Map[String, Any]): T
  }


  trait Serializer[T] extends JsonMethods {

    implicit val format = DefaultFormats
    
    def deserializ(jsonStr: String): T
  }


}

