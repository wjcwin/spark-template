package com.wjc.core

import com.wjc.listener.{SparkCoreListener, SparkSqlListener}
import org.apache.log4j.{Level, Logger}
import org.apache.spark.SparkConf
import org.apache.spark.serializer.KryoSerializer
import org.apache.spark.sql.SparkSession

trait Sparking {

  //self: {def main(args: Array[String]): Unit} =>

  // 屏蔽不必要的日志 ,在终端上显示需要的日志
  Logger.getLogger("org.apache.spark").setLevel(Level.WARN)
  Logger.getLogger("org.eclipse.jetty.server").setLevel(Level.WARN)
  Logger.getLogger("org.apache.kafka.clients.consumer").setLevel(Level.WARN)

  val conf: SparkConf = new SparkConf()
    .set("spark.serializer", classOf[KryoSerializer].getName)
    .set("spark.extraListeners", classOf[SparkCoreListener].getName)
    .set("hive.exec.dynamici.partition", "true")
    .set("hive.exec.dynamic.partition.mode", "nonstrict") //非严格模式
    .set("hive.exec.max.dynamic.partitions", "100000")
    .set("hive.exec.max.dynamic.partitions.pernode", "100000")
    .setAppName(this.getClass.getName.stripSuffix("$"))

  def enableLocalSupport(): Unit = conf.setMaster("local[*]")

  def enableSqlBloodSupport(): Unit = conf.set("spark.sql.queryExecutionListeners", classOf[SparkSqlListener].getName)

  def enableTaskMonitorSupport(): Unit = conf.set("enableSendMessageOnTaskFail", "true")

  def getSparkSession(uris: Option[String]): SparkSession = {
    val builder: SparkSession.Builder = SparkSession.builder().config(conf)
    if (uris.isDefined) {
      builder
        .config("hive.metastore.uris", uris.get)
        .enableHiveSupport()
    }
    builder.getOrCreate()
  }
}
