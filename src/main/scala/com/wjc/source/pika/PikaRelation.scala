package com.wjc.source.pika

import com.wjc.client.RedissonBucketClient
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.functions._
import org.apache.spark.sql.sources.{BaseRelation, InsertableRelation}
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{DataFrame, SQLContext}

/**
 * @author: 王建成
 * @since: 2024/1/30 17:23
 * @description: spark dataframe 自定义的pika输出源
 */
class PikaRelation(context: SQLContext,
                   params: Map[String, String],
                   userSchema: StructType
                  ) extends BaseRelation with InsertableRelation with Serializable {

  val SPERATOR: String = ","
  val PIKA_VALUE_JSON_SELECT_FIELDS: String = "jsonSelectFields"
  val PIKA_VALUE_JSON_FIELDS: String = "jsonFields"
  val PIKA_KEY_NAME: String = "keyColumn"

  val PIKA_MAX_PIPELINE_SIZE = "pika.max.pipeline.size"
  val PIKA_NODES = "pika.nodes"
  val PIKA_MASTER_NAME = "pika.master.name"
  val PIKA_AUTH = "jedis.auth"

  override def sqlContext: SQLContext = context

  override def schema: StructType = userSchema


  override def insert(data: DataFrame, overwrite: Boolean): Unit = {

    val jsonColumn = if (params.contains(PIKA_VALUE_JSON_FIELDS) && params(PIKA_VALUE_JSON_FIELDS).trim.nonEmpty) {
      data.col(params(PIKA_VALUE_JSON_FIELDS))
    } else if (params.contains(PIKA_VALUE_JSON_SELECT_FIELDS) && params(PIKA_VALUE_JSON_SELECT_FIELDS).trim.nonEmpty) {
      to_json(struct(params(PIKA_VALUE_JSON_SELECT_FIELDS).split(SPERATOR).map(data.col): _*))
    } else {
      throw new RuntimeException(s"option信息中需包含$PIKA_VALUE_JSON_FIELDS 或者 $PIKA_VALUE_JSON_SELECT_FIELDS 且其值不能为空")
    }
    val jsonResult = data.select(data.col(params(PIKA_KEY_NAME)).as("key"), jsonColumn.as("value"))


    val confBroadCast: Broadcast[Map[String, String]] = sqlContext.sparkContext.broadcast(params)
    jsonResult.foreachPartition(partition => {
      val kvs = partition.map(row =>
        (row.getAs[String]("key"),
          row.getAs[String]("value")
        )
      )

      //配置信息
      val confValue = confBroadCast.value
      val size = confValue.getOrElse(PIKA_MAX_PIPELINE_SIZE, 1000).toString.toInt
      val client = RedissonBucketClient(
        confValue(PIKA_NODES),
        confValue(PIKA_MASTER_NAME),
        confValue(PIKA_AUTH)
      ).redi

      kvs.toSeq.sliding(size, size).foreach((seq: Seq[(String, String)]) => {
        val result = seq.toMap
        val buckets = client.getBuckets
        import scala.collection.JavaConverters._
        buckets.set(result.asJava)
      })

      client.shutdown()

    })

  }
}
