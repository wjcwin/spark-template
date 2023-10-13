package com.wjc.utils

import java.util.ResourceBundle

import scala.collection.mutable

object ConfigsUtil {


  /** 配置文件 */
  private val bundle: ResourceBundle = ResourceBundle.getBundle("application")
  val env: String = bundle.getString("profile.active")
  private val ENV_PROPERTIES: ResourceBundle = ResourceBundle.getBundle(s"application-$env")

  /**数据库 链接信息 */
  lazy val PG_DRIVER: String = ENV_PROPERTIES.getString("pgDriver")
  lazy val MYSQL_DRIVER: String = ENV_PROPERTIES.getString("mysqlDriver")

  lazy val MYSQL_URL: String = ENV_PROPERTIES.getString("mysql.url")
  lazy val MYSQL_USER: String = ENV_PROPERTIES.getString("mysql.user")
  lazy val MYSQL_PASSWORD: String = ENV_PROPERTIES.getString("mysql.password")

  lazy val MYSQL_SUPERPARK = Map(
    "url" -> MYSQL_URL,
    "user" -> MYSQL_USER,
    "password" -> MYSQL_PASSWORD,
    "driver" -> MYSQL_DRIVER
  )

  /** hive 链接信息 */
  lazy val HIVE_METASTORES_URL: String = ENV_PROPERTIES.getString("hive.metastores.url")

  /** hdfs 配置信息 */
  lazy val DEFAULT_FS: String = ENV_PROPERTIES.getString("fs.defaultFS")
  lazy val NAME_SERVICES: String = ENV_PROPERTIES.getString("dfs.nameservices")
  lazy val HA_NAMENODES: String = ENV_PROPERTIES.getString("dfs.ha.namenodes.nameservices")
  lazy val nn1: String = ENV_PROPERTIES.getString("dfs.namenode.rpc-address.nameservices.nn1")
  lazy val nn2: String = ENV_PROPERTIES.getString("dfs.namenode.rpc-address.nameservices.nn2")
  lazy val DFS_PROVIDER: String = ENV_PROPERTIES.getString("dfs.client.failover.proxy.provider.keytopha")
  lazy val HDFS_URL: String = ENV_PROPERTIES.getString("hdfs.url")

  /** 企业微信群机器人 */
  lazy val webhook: String = ENV_PROPERTIES.getString("webhook")
  /** kafka 集群 */

  lazy val KAFKA_BROKER: String = ENV_PROPERTIES.getString("kafka.broker")
  lazy val ZOOKEEPER_SERVER: String = ENV_PROPERTIES.getString("zookeeper.server")
  /** 普罗米修斯 gateway */
  lazy val PROMETHEUS_URL: String = ENV_PROPERTIES.getString("prometheus.gate.way.url")

  /** pulsar */
  lazy val PULSAR_SERVICE_URL: String = ENV_PROPERTIES.getString("pulsar.service.url")

  /** es */
  lazy val ES_HOST: String = ENV_PROPERTIES.getString("es.host")
  lazy val ES_PORT: Int = ENV_PROPERTIES.getString("es.port").toInt
  lazy val ES_USER: String = ENV_PROPERTIES.getString("es.user")
  lazy val ES_PSW: String = ENV_PROPERTIES.getString("es.password")

  lazy val ES_CONFIG: mutable.Map[String, String] = mutable.Map(
    "es.index.auto.create" -> "true",
    "es.nodes.wan.only" -> "true",
    "es.nodes" -> s"$ES_HOST",
    "es.port" -> s"$ES_PORT",
    "es.net.http.auth.user" -> s"${ES_USER}",
    "es.net.http.auth.pass" -> s"${ES_PSW}",
    "es.batch.write.retry.count" -> "10",
    "es.batch.write.retry.wait" -> "60"
  )

  /** redis */
  lazy val REDIS_HOST: String = ENV_PROPERTIES.getString("redis.host")
  lazy val REDIS_PORT: String = ENV_PROPERTIES.getString("redis.port")
  lazy val REDIS_AUTH: String = ENV_PROPERTIES.getString("redis.auth")

  /** cos 配置信息*/
  lazy val COS_HDFS_BUCKET_NAME: String = ENV_PROPERTIES.getString("cos.hdfs.bucket_name")
  lazy val COS_SECRET_ID: String = ENV_PROPERTIES.getString("cos.secret_id")
  lazy val COS_SECRET_KEY: String = ENV_PROPERTIES.getString("cos.secret_key")
  lazy val COS_REGION: String = ENV_PROPERTIES.getString("cos.region")
  lazy val COS_APP_ID: String = ENV_PROPERTIES.getString("cos.app_id")



}
