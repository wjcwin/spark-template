package com.wjc.listener

import com.wjc.core.Logging
import org.apache.spark._
import org.apache.spark.scheduler.{SparkListener, SparkListenerApplicationEnd, SparkListenerApplicationStart, SparkListenerTaskEnd}
import org.joda.time.DateTime

import scala.collection.mutable.ArrayBuffer

class SparkCoreListener(conf:SparkConf) extends SparkListener with Logging {

  val msg = new ArrayBuffer[String]()

  override def onApplicationStart(applicationStart: SparkListenerApplicationStart): Unit = {
    val appId = applicationStart.appId.getOrElse("")
    val appName = applicationStart.appName
    val startTime = new DateTime(applicationStart.time).toString("yyyy-MM-dd HH:mm:ss")
    msg.append("应用程序ID：" + appId)
    msg.append("应用程序名称：" + appName)
    msg.append("应用程序开始时间：" + startTime)
  }

  override def onApplicationEnd(applicationEnd: SparkListenerApplicationEnd): Unit = {
    msg.append("应用程序结束时间：" + new DateTime(applicationEnd.time).toString("yyyy-MM-dd HH:mm:ss"))
    //通过邮件或者钉钉（webhook）的方式发送
  }

  /**
   * 监控task异常信息
   */
  override def onTaskEnd(taskEnd: SparkListenerTaskEnd): Unit = {
    val flag = conf.getBoolean("enableSendMessageOnTaskFail", defaultValue = false)
    if(flag) {
      val info = taskEnd.taskInfo
      if (info != null && taskEnd.stageAttemptId != -1) {
        val errorMessage = taskEnd.reason match {
          case e: ExceptionFailure => Some(e.toErrorString)
          case e: TaskFailedReason => Some(e.toErrorString)
          case _ => None
        }
        if(errorMessage.isDefined) {
          //do something
        }
      }
    }
  }
}
