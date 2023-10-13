package com.wjc.utils

import scala.collection.TraversableLike

/**
  * @program: com.wjc.utils->Breakeable
  * @description: 描述
  * @author: wangjiancheng
  * @create: 2020/9/2 14:20
  **/
//noinspection ScalaDocUnknownTag
case class Breakable() {
  def break(): Unit = throw BreakException

  def continue(): Unit = throw ContinueException

  def foreach(t: TraversableLike[_, _], op: Any => Unit): Unit = {
    try {
      t.foreach(i => {
        try {
          op(i)
        } catch {
          case ex: Exception =>
            if (ex != ContinueException) throw ex
        }
      })
    } catch {
      case ex: Exception =>
        if (ex != BreakException) throw ex
    }
  }

  object BreakException extends Exception

  object ContinueException extends Exception

}
