package com.wjc.utils

import com.ctrip.framework.apollo.{Config, ConfigService}

/**
  * @program: com.wjc.utils->Apollos
  * @description: 描述
  * @author: wangjiancheng
  * @create: 2020/9/2 10:00
  **/
//noinspection ScalaDocUnknownTag
object Apollos {

  System.setProperty("app.id", "gy-tocc-process")
  System.setProperty("apollo.meta", "http://10.11.56.12:8080")
  val config: Config = ConfigService.getConfig("application")
  //kafka相关
  val kafkaServer: String = config.getProperty("kafka.bootstrap.server", null)
  val zkServer: String = config.getProperty("kafka.zookeeper.server", null)
  val topic: Array[String] = config.getProperty("kafka.test.topic", null).split(",")
  val group: String = config.getProperty("kafka.test.group", null)
  //hive相关
  var hiveDriver: String = config.getProperty("hive.driver", null)
  var hiveURL: String = config.getProperty("hive.url", null)
  var hiveUserName: String = config.getProperty("hive.username", null)
  var hivePassWord: String = config.getProperty("hive.password", null)
  //hdfs相关
  var hdfsURl:String=config.getProperty("hdfs.url",null)

}
