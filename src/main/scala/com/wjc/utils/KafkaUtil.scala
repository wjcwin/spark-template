package com.wjc.utils

import java.util.{Collections, Properties}

import org.apache.kafka.clients.consumer.{ConsumerConfig, KafkaConsumer}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig}
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}

/**
  * @program: com.wjc.utils->KafkaUtil
  * @description: 描述
  * @author: wangjiancheng
  * @create: 2020/9/2 10:13
  **/
//noinspection ScalaDocUnknownTag
object KafkaUtil {

  /**
    * 获取Kafka生产者
    * @param brokerList broker信息
    * @return
    */
  def getKafkaProducer(brokerList: String): KafkaProducer[String, String] = {
    val properties = new Properties()
    properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerList)
    properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer].getName) //key的序列化;
    properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer].getName) //value的序列化;
    properties.put(ProducerConfig.ACKS_CONFIG, "1") //消息反馈;
    val producer4Kafka = new KafkaProducer[String, String](properties)
    producer4Kafka
  }

  /**
    * 获取Kafka的消费者
    * @param brokerList broker信息
    * @param topic topic信息
    * @param consumerGroupId 消费组组
    * @return
    */
  def getKafkaConsumer(brokerList: String, topic: String, consumerGroupId: String): KafkaConsumer[String, String] = {
    val properties = new Properties()
    properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerList)
    properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, classOf[StringDeserializer].getName) //key的序列化;
    properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, classOf[StringDeserializer].getName) //value的序列化;
    properties.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId) //指定groupid
    val consumer4Kafka = new KafkaConsumer[String, String](properties)
    consumer4Kafka.subscribe(Collections.singletonList(topic))
    consumer4Kafka
  }

}
