package com.wjc.core

import com.wjc.utils.TimeUtil
import org.apache.commons.lang3.time.StopWatch
import org.joda.time.DateTime

/**
 * @author: 王建成
 * @since: 2023/10/13 16:57
 * @description: 描述
 */
trait SparkJob extends Sparking {
  private val startTime = new DateTime()
  val stopWatch: StopWatch = StopWatch.createStarted()
  def main(args: Array[String]): Unit = {
    logStarting()
    handle(args)
    logEnding()
  }

  /**
   * 子类必须实现handle方法，在该方法内实现业务代码
   */
  def handle(args: Array[String]):Unit

  /**
   * 打印任务开始日志
   */
  def logStarting(): Unit = {
    warn(s">>>>>任务开始时间:${startTime.toString("yyyy-MM-dd HH:mm:ss.SSS")}")
  }
  /**
   * 打印任务结束日志
   */
  def logEnding(): Unit = {
    val endTime = new DateTime()
    warn(s"<<<<<任务开始时间:${startTime.toString("yyyy-MM-dd HH:mm:ss.SSS")}")
    warn(s"<<<<<任务结束时间: ${endTime.toString("yyyy-MM-dd HH:mm:ss.SSS")}")
    warn(s"<<<<<任务耗时: ${TimeUtil.getDateInterVal(startTime, endTime)}")
    warn(s"<<<<<任务耗时: ${stopWatch.toString}秒")
  }

}
