package com.wjc.source.pika

import org.apache.spark.sql.sources.{BaseRelation, CreatableRelationProvider, DataSourceRegister}
import org.apache.spark.sql.{DataFrame, SQLContext, SaveMode}

/**
 * @author: 王建成
 * @since: 2024/1/30 17:49
 * @description: spark dataframe 自定义的pika输出源
 *               【数据血缘】自定义pika-source继承spark的读写特质
 *               https://ktsoft.coding.net/p/BigDataCenter/subtasks/issues/6244/detail
 */
class DefaultSource extends CreatableRelationProvider with DataSourceRegister  with Serializable {

  val SPERATOR: String = ","
  val PIKA_VALUE_JSON_SELECT_FIELDS: String = "jsonSelectFields"
  val PIKA_VALUE_JSON_FIELDS: String = "jsonFields"
  val PIKA_KEY_NAME: String = "keyColumn"

  val PIKA_MAX_PIPELINE_SIZE = "pika.max.pipeline.size"
  val PIKA_NODES = "pika.nodes"
  val PIKA_MASTER_NAME = "pika.master.name"
  val PIKA_AUTH = "jedis.auth"
  override def createRelation(sqlContext: SQLContext, mode: SaveMode, parameters: Map[String, String], data: DataFrame): BaseRelation = {
    checkOption(parameters)
    val relation = new PikaRelation(sqlContext, parameters, data.schema)
    relation.insert(data, true)
    relation
  }

  override def shortName(): String = "pika"

  private def checkOption(parameters: Map[String, String]): Unit ={

    assert(parameters.contains(PIKA_NODES) && parameters(PIKA_NODES).trim.nonEmpty, s"option信息中需包含$PIKA_NODES ")
    assert(parameters.contains(PIKA_MASTER_NAME) && parameters(PIKA_MASTER_NAME).trim.nonEmpty, s"option信息中需包含$PIKA_MASTER_NAME ")

    assert(parameters.contains(PIKA_KEY_NAME) && parameters(PIKA_KEY_NAME).trim.nonEmpty, s"option信息中需包含$PIKA_KEY_NAME ")
    assert(parameters.contains(PIKA_VALUE_JSON_FIELDS) || parameters.contains(PIKA_VALUE_JSON_SELECT_FIELDS), s"option信息中需包含$PIKA_VALUE_JSON_FIELDS 或者 $PIKA_VALUE_JSON_SELECT_FIELDS")


  }

}
