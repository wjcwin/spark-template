package com.wjc.core

import kafka.api.{OffsetRequest, PartitionOffsetRequestInfo}
import kafka.cluster.{Broker, BrokerEndPoint}
import kafka.common.TopicAndPartition
import kafka.consumer.SimpleConsumer
import kafka.utils.{ZKGroupTopicDirs, ZkUtils}
import org.I0Itec.zkclient.ZkClient
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.protocol.SecurityProtocol
import org.apache.spark.streaming.kafka010.OffsetRange

import scala.collection.mutable

class OffsetZk(func: () => ZkClient) extends Logging with Serializable {

  lazy val zkClient: ZkClient = func()
  lazy val zkUtils: ZkUtils = ZkUtils.apply(zkClient, isZkSecurityEnabled = false)

  def getBeginOffset(topics: Seq[String], groupId: String): mutable.HashMap[TopicPartition, Long] = {
    val fromOffsets = mutable.HashMap.empty[TopicPartition, Long]
    val partitionMap: mutable.Map[String, Seq[Int]] = zkUtils.getPartitionsForTopics(topics)
    partitionMap.foreach { topicPartitions: (String, Seq[Int]) =>
      val topic: String = topicPartitions._1
      val topicDirs = new ZKGroupTopicDirs(groupId, topic)
      topicPartitions._2.foreach { partition: Int =>
        val tp = new TopicPartition(topic, partition)
        //获取Kafka中最早偏移量
        val kafkaOffset_left: Long = getOffsetForKafka(tp)
        //获取Kafka中最新偏移量
        val kafkaOffset_right: Long = getOffsetForKafka(tp, OffsetRequest.LatestTime)
        val zkPath = s"${topicDirs.consumerOffsetDir}/$partition"
        zkUtils.makeSurePersistentPathExists(zkPath)
        Option(zkUtils.readData(zkPath)._1) match {
          case Some(zkOffset) =>
            //zk中保存的偏移量与 Kafka中的偏移量进行比较，防止任务长期未启动后再启动造成out off Kafka offset ranges
            if (zkOffset.toLong < kafkaOffset_left) { //头部越界
              fromOffsets += tp -> kafkaOffset_left
            } else if (zkOffset.toLong > kafkaOffset_right) { //尾部越界
              fromOffsets += tp -> kafkaOffset_right
            } else fromOffsets += tp -> zkOffset.toLong //正常情况
          case None => fromOffsets += tp -> kafkaOffset_left
        }
      }
    }
    fromOffsets
  }

  def saveEndOffset(offsetRanges: Array[OffsetRange], groupId: String): Unit = {
    offsetRanges.foreach { offsetRange: OffsetRange =>
      val topicDirs = new ZKGroupTopicDirs(groupId, offsetRange.topic)
      val zkPath = s"${topicDirs.consumerOffsetDir}/${offsetRange.partition}"
      zkUtils.updatePersistentPath(zkPath, offsetRange.untilOffset.toString)
    }
  }

  def getOffsetForKafka(topicPartition: TopicPartition, time: Long = OffsetRequest.EarliestTime): Long = {
    val brokerId: Int = zkUtils.getLeaderForPartition(topicPartition.topic, topicPartition.partition).get
    val broker: Broker = zkUtils.getBrokerInfo(brokerId).get
    val endpoint: BrokerEndPoint = broker.getBrokerEndPoint(SecurityProtocol.PLAINTEXT)
    val consumer = new SimpleConsumer(endpoint.host, endpoint.port, 10000, 100000, "getOffset")
    val tp = TopicAndPartition(topicPartition.topic, topicPartition.partition)
    val request = OffsetRequest(Map(tp -> PartitionOffsetRequestInfo(time, 1)))
    consumer.getOffsetsBefore(request).partitionErrorAndOffsets(tp).offsets.head
  }
}

object OffsetZk extends Logging {

  def apply(zkServer: String): OffsetZk = {
    val func = () => {
      val zkClient = ZkUtils.createZkClient(zkServer, 30000, 30000)
      //钩子，jvm退出时关闭客户端，释放资源
      sys.addShutdownHook {
        info("Execute hook thread: ZkManager")
        zkClient.close()
      }
      zkClient
    }
    new OffsetZk(func)
  }
}