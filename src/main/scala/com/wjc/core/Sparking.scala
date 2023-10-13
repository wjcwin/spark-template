package com.wjc.core

import com.wjc.listener.SparkCoreListener
import com.wjc.utils.ConfigsUtil
import org.apache.log4j.{Level, Logger}
import org.apache.spark.SparkConf
import org.apache.spark.serializer.KryoSerializer
import org.apache.spark.sql.SparkSession
import org.joda.time.DateTime

trait Sparking extends Logging {

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
    .set("spark.sql.crossJoin.enabled", "true")
    .set("spark.debug.maxToStringFields", "1000")
    .set("spark.sql.parquet.mergeSchema", "true")
    .set("spark.sql.legacy.allowCreatingManagedTableUsingNonemptyLocation", "true")
    .set("spark.hadoop.io.compression.codecs", "org.apache.hadoop.io.compress.DefaultCodec")
    .setAppName(this.getClass.getName.stripSuffix("$"))

  /**
   * 用于本地调试
   */
  def enableLocalSupport(): Unit = conf.setMaster("local[*]")

  /**
   * 开启 SparkLens （https://github.com/qubole/sparklens.git） ：一个可以在spark任务结束后 分析任务执行情况，资源使用情况并提出优化建议
   * spark.sparklens.data.dir：将sparklens结果存储在 指定的目录下，可换为其它分布式文件系统或者对象存储
   *
   */
  def useSparkLens(): Unit = {
    warn("开启 sparkLens")
    conf.set("spark.extraListeners", "com.qubole.sparklens.QuboleJobListener")
    conf.set("spark.sparklens.data.dir",
      s"hdfs://hadoopHA/user/spark/sparklens/${new DateTime().toString("yyyyMMdd")}/${this.getClass.getName}")
  }

  /**
   * 开启 JvmProfiler（https://github.com/uber-common/jvm-profiler.git）
   *
   * @param metricInterval jvm数据采集间隔时间
   * spark.executor.extraJavaOptions：通过指定executor的启动Java参数 来开启 jvm数据采集，每隔 metricInterval 毫秒就将结果发送到 kafka
   *
   */
  def useJvmProfiler(metricInterval: Long = 10000): Unit = {
    warn("开启 JvmProfiler")
    conf.set("spark.executor.extraJavaOptions",
      s"-javaagent:/opt/spark/jars/jvm-profiler-1.0.0.jar=reporter=com.uber.profiling.reporters.KafkaOutputReporter,metricInterval=$metricInterval,brokerList=kafka001:9092,topicPrefix=spark_executor_jvm_profiler")
  }

  /**
   * 开启 SparkLine解析血缘关系 (https://github.com/AbsaOSS/spline-spark-agent.git)
   *
   */
  def useSparkLine(): Unit = {
    warn("开启 SparkLine解析血缘关系")
    conf.set("spark.sql.queryExecutionListeners", "za.co.absa.spline.harvester.listener.SplineQueryExecutionListener")
    conf.set("spark.spline.lineageDispatcher", "kafka")
    conf.set("spark.spline.mode", "ENABLED")
    conf.set("spark.spline.plugins.za.co.absa.spline.harvester.plugin.embedded.NonPersistentActionsCapturePlugin.enabled", "true")
    conf.set("spline.lineageDispatcher.kafka.topic", "sparkLine")
    conf.set("spline.lineageDispatcher.kafka.producer.bootstrap.servers", ConfigsUtil.KAFKA_BROKER)
  }

  def enableTaskMonitorSupport(): Unit = conf.set("enableSendMessageOnTaskFail", "true")

  /**
   * 通过指定 hive 的 metastore.uris 来创建 SparkSession
   * @param uris hive.metastore.uris
   * @return
   */
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
