package com.wjc.job.realtime

import java.io.PrintWriter
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import com.wjc.client.HDFSClient
import com.wjc.core.{HdfsConf, Logging, OffsetZk, SparkStreaming}
import com.wjc.listener.SparkStreamingListener
import com.wjc.utils.Apollos
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.TopicPartition
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.InputDStream
import org.apache.spark.streaming.kafka010.{HasOffsetRanges, OffsetRange}
import org.spark_project.jetty.server.handler.{AbstractHandler, ContextHandler}
import org.spark_project.jetty.server.{Request, Server}

import scala.collection.mutable

object StreamingExample extends SparkStreaming with Logging with HdfsConf {
  val hdfs: FileSystem = HDFSClient(Apollos.hdfsURl, hdfsConf).fs

  def run(): Unit = {

    enableLocalSupport() //本地调试
    enableTaskMonitorSupport() //task监控

    val kafkaParams: Map[String, Object] = getKafkaParams(Apollos.kafkaServer, Apollos.group)

    val offsetZk = OffsetZk(Apollos.zkServer)
    val offsets: mutable.HashMap[TopicPartition, Long] = offsetZk.getBeginOffset(Apollos.topic, Apollos.group)
    println(offsets)
    //批次时间通过spark-submit脚本 中的 --conf spark.batch.time=60 传进来
    val ssc: StreamingContext = setupSsc(None, conf.getInt("spark.batch.time", 1))
    ssc.addStreamingListener(new SparkStreamingListener(ssc))
    val input: InputDStream[ConsumerRecord[String, String]] = setupStream(ssc, Apollos.topic, kafkaParams, offsets)

    var offsetRanges = Array.empty[OffsetRange]
    input.transform { rdd: RDD[ConsumerRecord[String, String]] =>
      offsetRanges = rdd.asInstanceOf[HasOffsetRanges].offsetRanges
      rdd
    }.map((x: ConsumerRecord[String, String]) => x.value()).foreachRDD { rdd: RDD[String] =>
      if (!rdd.isEmpty()) {
        //业务代码
        rdd.foreach(println(_))
        //保存偏移量
        offsetZk.saveEndOffset(offsetRanges, Apollos.group)
      }
    }
    ssc.start()
    //daemonHttpServer(15555,ssc)  //通过Http方式优雅的关闭策略
    stopByMarkFile(ssc) //方式二通过扫描HDFS文件来优雅的关闭
    ssc.awaitTermination()
  }

  /** *
    * 通过一个消息文件来定时触发是否需要关闭流程序
    *
    * @param ssc StreamingContext
    */
  def stopByMarkFile(ssc: StreamingContext): Unit = {
    val intervalMills: Int = 10 * 1000 // 每隔10秒扫描一次消息是否存在
    var isStop = false
    val hdfs_file_path = s"/spark/streaming/stop/${this.getClass.getName}" //判断消息文件是否存在，如果存在就
    while (!isStop) {
      isStop = ssc.awaitTerminationOrTimeout(intervalMills)
      if (!isStop && isExistsMarkFile(hdfs_file_path)) {
        warn("2秒后开始关闭sparstreaming程序.....")
        Thread.sleep(2000)
        ssc.stop(stopSparkContext = true, stopGracefully = true)
      }
    }
  }

  /** *
    * 判断是否存在mark file
    *
    * @param hdfs_file_path mark文件的路径
    * @return
    */
  def isExistsMarkFile(hdfs_file_path: String): Boolean = {
    hdfs.exists(new Path(hdfs_file_path))
  }

  /** **
    * 负责启动守护的jetty服务
    *
    * @param port 对外暴露的端口号
    * @param ssc  Stream上下文
    */
  def daemonHttpServer(port: Int, ssc: StreamingContext): Unit = {
    val server = new Server(port)
    val context = new ContextHandler()
    val name: String = this.getClass.getName
    context.setContextPath(s"/close/$name")
    context.setHandler(new CloseStreamHandler(ssc))
    server.setHandler(context)
    server.start()
    server.join()
  }

  /** * 负责接受http请求来优雅的关闭流
    *
    * @param ssc Stream上下文
    */
  class CloseStreamHandler(ssc: StreamingContext) extends AbstractHandler {
    override def handle(s: String, baseRequest: Request, req: HttpServletRequest, response: HttpServletResponse): Unit = {
      warn("开始关闭......")
      ssc.stop(true, true) //优雅的关闭
      response.setContentType("text/html; charset=utf-8")
      response.setStatus(HttpServletResponse.SC_OK)
      val out: PrintWriter = response.getWriter
      out.println("close success")
      baseRequest.setHandled(true)
      warn("关闭成功.....")
    }
  }

}
