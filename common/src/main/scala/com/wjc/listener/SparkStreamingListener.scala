package com.wjc.listener

import com.wjc.core.Logging
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.scheduler.{StreamingListener, StreamingListenerBatchStarted}
import org.joda.time.DateTime

import scala.collection.mutable.ArrayBuffer

/**
  * 监控spark-streaming
  */
class SparkStreamingListener(ssc:StreamingContext) extends StreamingListener with Logging {

  val msg = new ArrayBuffer[String]()
  msg.append("应用程序ID：" + ssc.sparkContext.applicationId)
  msg.append("应用程序名称：" + ssc.sparkContext.appName)
  msg.append("应用程序开始时间：" + new DateTime(ssc.sparkContext.startTime).toString("yyyy-MM-dd HH:mm:ss"))

  override def onBatchStarted(batchStarted: StreamingListenerBatchStarted): Unit = {
    //当前调度等待时间
    val Delay_ts = batchStarted.batchInfo.schedulingDelay.get / (1000 * 60D)
    //todo 触发报警条件和报警动作
  }
}
