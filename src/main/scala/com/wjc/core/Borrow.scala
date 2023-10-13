package com.wjc.core

import scala.util.control.Exception.ignoring

/**
 * 借贷模式
  * 名字调用
  * 保证资源自动回收
  * 通过using的抽象控制
 */
trait Borrow {

  type closeable = { def close(): Unit }

  def using[R <: closeable, T](resource: =>R)(execute: R => T): T = {
    var res: R = null.asInstanceOf[R]
    try {
      res = resource
      execute(res)
    }finally {
      ignoring(classOf[Throwable]) apply resource.close()
    }
  }
}
