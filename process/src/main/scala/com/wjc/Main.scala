package com.wjc

import com.wjc.core.Logging

/**
  * @program: com.wjc->Main
  * @description: 描述
  * @author: wangjiancheng
  * @create: 2020/9/2 14:47
  **/
//noinspection ScalaDocUnknownTag
object Main extends Logging{

  def main(args: Array[String]): Unit = {
    assert(args != null && args.length >= 2, "请指定执行的类路径 和 执行方法 ，如：com.hikcreate.job.scheduling.SchedulePlan run")
    val classPath: String = args.head
    val methodName: String = args(1)
    val clazz: Class[_] = Class.forName(classPath)
    val methods: Array[String] = clazz.getMethods.map(m => m.getName)
    assert(methods.contains(methodName), "指定的方法名"+methodName+"有误，你可能是要指定以下：" + methods.toList.mkString(","))
    info(s"即将执行$classPath 的 $methodName 方法......")
    if (args.length >= 3) {
      val methodArgs: Array[String] = args.drop(2)
      clazz.getMethod(args(1), classOf[Array[String]]).invoke(null, methodArgs)
    } else {
      clazz.getMethod(args(1)).invoke(null)
    }
  }

}
