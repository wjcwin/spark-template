package com.wjc.core

import java.io.{BufferedReader, InputStreamReader}
import java.util.Collections

import com.alibaba.fastjson.{JSON, JSONObject}
import com.wjc.client.HDFSClient
import com.wjc.utils.{ConfigsUtil, KafkaUtil, WxMonitor}
import org.apache.hadoop.fs.Path
import org.apache.kafka.common.TopicPartition
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.types.{DataTypes, StructField, StructType}
import org.joda.time.DateTime

import scala.collection.mutable

/**
 * @author: 王建成
 * @since: 2023/10/17 16:15
 * @description: 读写hudi
 */
trait Hudiing extends Logging {
  private val basePath = "cosn://hdfs-1309102445/user/hudi"
  private val chkMsgMap = mutable.Map[String, String]()

  /**
   * 根据库名表名获取 相对的hudi表
   *
   * @param spark      spark session
   * @param tableName  库名.表名
   * @param partitions 分区
   * @return
   */
  def getHudi(spark: SparkSession, tableName: String, partitions: String): DataFrame = {
    chkHudiDelay(spark, tableName)
    warn(s"获取数据湖 $tableName 的 $partitions 分区数据")
    val dbTable = tableName.replaceAll("\\s", "")
    val db = dbTable.split("\\.")(0)
    val table = dbTable.split("\\.")(1)
    var tablePath = basePath + s"/$db/$table"
    tablePath = tablePath + s"/$partitions"

    var df = try {
      spark.read.format("org.apache.hudi")
        .load(tablePath + "/*")
      //        .drop("_hoodie_commit_seqno", "_hoodie_record_key", "_hoodie_partition_path", "_hoodie_file_name", "_event_time")

    }
    if (df.schema.exists(sf => sf.dataType == DataTypes.TimestampType)) {
      val swithcSchema = switchSchema(df)
      df = getHudi(spark, tablePath, swithcSchema)
    }

    df
  }

  /**
   * 根据库名表名获取 相对的hudi表
   *
   * @param spark        spark session
   * @param tableName    库名.表名
   * @param partitionSet 分区
   * @return
   */
  def getHudi(spark: SparkSession, tableName: String, partitionSet: Set[String]): DataFrame = {
    val dbTable = tableName.replaceAll("\\s", "")

    getHudi(spark, dbTable, s"{${partitionSet.mkString(",")}}")

  }

  /**
   * 根据库名表名获取 相对的hudi表
   *
   * @param spark      spark session
   * @param tableName  库名.表名
   * @param partitions 分区
   * @param fields     指定字段
   * @return
   */
  def getHudi(spark: SparkSession, tableName: String, partitions: String, fields: Seq[String]): DataFrame = {
    chkHudiDelay(spark, tableName)
    import org.apache.spark.sql.functions
    val dbTable = tableName.replaceAll("\\s", "")
    val db = dbTable.split("\\.")(0)
    val table = dbTable.split("\\.")(1)
    var tablePath = basePath + s"/$db/$table"

    tablePath = tablePath + s"/$partitions"
    val cols = fields.map(functions.col)
    val df = try {
      spark.read.format("org.apache.hudi")
        .load(tablePath + "/*")
        .select(cols: _*)
    }

    val orgSchema = df.schema
    val swithcSchema = StructType(orgSchema.map(f =>
      if (f.dataType == DataTypes.TimestampType) {
        StructField(f.name, DataTypes.LongType)
      } else {
        f
      }
    ))
    val result = getHudi(spark, tablePath, swithcSchema)
    result
  }

  /**
   * 根据库名表名获取 相对的hudi表
   *
   * @param spark     spark session
   * @param tableName 库名.表名
   * @return
   */
  def getHudi(spark: SparkSession, tableName: String): DataFrame = {
    chkHudiDelay(spark, tableName)
    val dbTable = tableName.replaceAll("\\s", "")
    val db = dbTable.split("\\.")(0)
    val table = dbTable.split("\\.")(1)
    val tablePath = basePath + s"/$db/$table/*"
    var df = try {
      spark.read.format("org.apache.hudi")
        .load(tablePath + "/*")
      //        .drop("_hoodie_commit_time", "_hoodie_commit_seqno", "_hoodie_record_key", "_hoodie_partition_path", "_hoodie_file_name", "_event_time")
    }

    if (df.schema.exists(sf => sf.dataType == DataTypes.TimestampType)) {
      val swithcSchema = switchSchema(df)
      df = getHudi(spark, tablePath, swithcSchema)
    }
    df

  }

  /**
   * 根据库名表名获取 相对的hudi表
   *
   * @param spark     spark session
   * @param tableName 库名.表名
   * @return
   */
  def getHudiNoPartition(spark: SparkSession, tableName: String): DataFrame = {
    chkHudiDelay(spark, tableName)
    val dbTable = tableName.replaceAll("\\s", "")
    val db = dbTable.split("\\.")(0)
    val table = dbTable.split("\\.")(1)
    val tablePath = basePath + s"/$db/$table"
    var df = try {
      spark.read.format("org.apache.hudi")
        .load(tablePath + "/*")
      //        .drop("_hoodie_commit_time", "_hoodie_commit_seqno", "_hoodie_record_key", "_hoodie_partition_path", "_hoodie_file_name", "_event_time")
    }
    if (df.schema.exists(sf => sf.dataType == DataTypes.TimestampType)) {
      val swithcSchema = switchSchema(df)
      df = getHudi(spark, tablePath, swithcSchema)
    }
    df
  }

  /**
   * 根据hudi路径 ，指定的schema获取hudi数据
   *
   * @param spark      sparksession
   * @param path       hudi路径
   * @param dataSchema 指定schema
   * @return
   */
  def getHudi(spark: SparkSession, path: String, dataSchema: StructType): DataFrame = {
    val df: DataFrame = try {
      spark.read.format("org.apache.hudi")
        .schema(dataSchema)
        .load(path + "/*")
    }

    df
  }

  /**
   * 转换 schema，将hudi表中存储的 TimestampType 转为 LongType
   *
   * @param df        dataframe
   * @param addFields 可能增加字段
   * @return 转换后的schema
   */
  def switchSchema(df: DataFrame, addFields: Seq[StructField] = null): StructType = {
    val orgSchema = df.schema
    val columns = df.columns
    val fields: Seq[StructField] = orgSchema.map(f =>
      if (f.dataType == DataTypes.TimestampType) {
        StructField(f.name, DataTypes.LongType)
      } else {
        f
      }
    )
    var swithcSchema = StructType(fields)

    if (addFields != null && addFields.nonEmpty) {
      addFields.foreach(f => {
        if (!columns.contains(f.name)) {
          swithcSchema = swithcSchema.add(f)
        }
      })
    }
    swithcSchema
  }

  /**
   * 根据 hudi目录里 最新 commit文件中 deltastreamer.checkpoint 保存的Kafka topic:partition:offset信息来计算hudi表中数据延迟了多少
   *
   * @param spark     sparksession
   * @param tableName hudi表名
   */
  def chkHudiDelay(spark: SparkSession, tableName: String): Unit = {
    try {
      if (chkMsgMap.contains(tableName)) {
        val existMsg = JSON.parseObject(chkMsgMap.getOrElse(tableName, "{}"))
        warn(
          s"""
             |请求时间：${existMsg.getString("requestTime")}
             |Kafka-topic：${existMsg.getString("topic")}
             |Kafka-offset：${existMsg.getLong("beginningOffset")}~${existMsg.getLong("endOffset")}；共${existMsg.getLong("endOffset") - existMsg.getLong("beginningOffset")}条
             |Hudi-表：$tableName
             |Hudi-chk-offset：${existMsg.getLong("offset")}
             |Hudi-last-commit-file：${existMsg.getString("lastCommit")}
             |延迟堆积的Kafka Record数量：${existMsg.getLong("delayOffset")}条
             |""".stripMargin)

      } else {
        val dbTable = tableName.replaceAll("\\s", "")
        val db = dbTable.split("\\.")(0)
        val table = dbTable.split("\\.")(1)
        val tablePath = basePath + s"/$db/$table"

        //1 获取hudi中的 deltastreamer.checkpoint.key
        val conf = spark.sparkContext.hadoopConfiguration
        val client = HDFSClient("cosn://hdfs-1309102445", conf)
        val statuses = client.listStatus(new Path(s"$tablePath/.hoodie/"))
        val lastCommit = statuses
          .filter(f => f.getPath.getName.endsWith(".commit"))
          .maxBy(f => f.getModificationTime)
        warn(s"查询的表：$tableName，当前最新commit文件：$lastCommit")
        val br: BufferedReader = new BufferedReader(new InputStreamReader(client.open(lastCommit.getPath)))
        val context = br.lines().toArray.mkString("\n")
        val extraMetaData = JSON.parseObject(context).getJSONObject("extraMetadata")


        if (extraMetaData.containsKey("deltastreamer.checkpoint.key")) {
          //获取 hudi .hoodie文件中 chk 的 topic offset
          val chk = extraMetaData.getString("deltastreamer.checkpoint.key")
          val topic = chk.split(",").head
          val partitionWithOffset = chk.split(",")(1)
          val offset = partitionWithOffset.split(":")(1).toLong

          //2 获取 Kafka topic max-offset min-offset
          val kafkaConsumer = KafkaUtil.getKafkaConsumerNoSub(ConfigsUtil.KAFKA_BROKER, topic, topic + "_delay_chk")
          val topicPartition = new TopicPartition(topic, 0)
          val list = Collections.singletonList(topicPartition)
          kafkaConsumer.assign(list)
          kafkaConsumer.seekToBeginning(list)
          val beginningOffset = kafkaConsumer.position(topicPartition)

          kafkaConsumer.seekToEnd(list)
          val endOffset = kafkaConsumer.position(topicPartition)

          val warnMsg =
            s"""
               |请求时间：${new DateTime().toString("yyyy-MM-dd HH:mm:ss")}
               |Kafka topic：$topic
               |Kafka offset：$beginningOffset~$endOffset；共${endOffset - beginningOffset}条
               |Hudi 表：$tableName
               |Hudi-chk-offset：$offset
               |Hudi-last-commit-file：$lastCommit
               |延迟堆积的Kafka Record数量：${endOffset - offset}条
               |""".stripMargin
          warn(warnMsg)
          val json = new JSONObject()
          json.put("requestTime", new DateTime().toString())
          json.put("topic", topic)
          json.put("beginningOffset", beginningOffset)
          json.put("endOffset", endOffset)
          json.put("offset", offset)
          json.put("lastCommit", lastCommit.toString)
          json.put("delayOffset", endOffset - offset)
          chkMsgMap.put(tableName, json.toJSONString)
          if (
            ((endOffset - offset) > 1000 && ((endOffset - offset) / (endOffset - beginningOffset + 1) > 0.3))
              || (endOffset > 10000 && (System.currentTimeMillis() - lastCommit.getModificationTime) > 24 * 3600 * 1000L)
          ) {
            WxMonitor.pushMessage(
              s"""
                 |spark离线任务：${spark.conf.get("spark.app.name")} 在获取${tableName}时发现延迟过高
                 |$warnMsg
                 |""".stripMargin, "https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=sdfd-23-ghfgh-cvbcv-as")
          }
        } else {
          warn("在最新的commit文件中未能找到 extraMetadata#deltastreamer.checkpoint.key，可能是Flink入湖产生的hudi表")
        }
      }
    } catch {
      case e: Throwable =>
        e.printStackTrace()
        warn("计算Hudi表延迟堆积出错")
    }

  }

}
