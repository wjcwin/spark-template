package com.wjc.core

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.streaming.dstream.InputDStream
import org.apache.spark.streaming.kafka010.{ConsumerStrategies, KafkaUtils, LocationStrategies}
import org.apache.spark.streaming.{Seconds, StreamingContext}

import scala.collection.mutable

trait SparkStreaming extends Sparking {

  //self:{ def main(args: Array[String]):Unit } =>

  conf.set("spark.streaming.stopGracefullyOnShutdown","true")//优雅关闭
    .set("spark.speculation", "true") //推断执行
    .set("spark.speculation.interval", "600s") //推断执行触发时间
    .set("spark.speculation.quantile", "0.9") //推断执行触发百分比
  def getKafkaParams(servers:String, groupId: String): Map[String,Object] = {
    Map[String,Object](
      "bootstrap.servers" -> servers,
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "group.id"-> groupId,
      "enable.auto.commit" -> (false:java.lang.Boolean)
    )
  }

  def setupSsc(uris: Option[String], second:Int) = new StreamingContext(getSparkSession(uris).sparkContext,Seconds(second))

  def setupStream(ssc:StreamingContext,
                  topics:Array[String],
                  kafkaParams:Map[String, Object],
                  fromOffset:mutable.HashMap[TopicPartition,Long]):InputDStream[ConsumerRecord[String,String]] ={
    KafkaUtils.createDirectStream[String, String](
      ssc,
      LocationStrategies.PreferConsistent,
      ConsumerStrategies.Subscribe[String, String](topics,kafkaParams,fromOffset)
    )
  }
}
