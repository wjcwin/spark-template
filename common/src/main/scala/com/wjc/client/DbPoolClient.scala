package com.wjc.client

import com.wjc.core.{Borrow, Logging}
import scalikejdbc.{ConnectionPool, ConnectionPoolSettings, DB}

/**
  *jdbc 连接池工具
  */
object DbPoolClient extends Borrow with Logging {

  /**
    *
    * @param poolName 连接池名字
    * @param driver   驱动
    * @param url      数据库连接url
    * @param username 用户名
    * @param password 密码
    */
  def init(poolName: String, driver: String, url: String, username: String, password: String): Unit = {
    if (!ConnectionPool.isInitialized(Symbol(poolName))) {
      Class.forName(driver)
      val settings = ConnectionPoolSettings(
        initialSize = 3,
        maxSize = 5,
        connectionTimeoutMillis = 3000L,
        validationQuery = "select 1")
      ConnectionPool.add(Symbol(poolName), url, username, password, settings)
      info(s"Initialize connection pool：$poolName")
    }
  }

  /**
    *
    * @param poolName 连接池名称
    * @param driver   驱动
    * @param url      数据库连接url
    * @param username 用户名
    * @param password 密码
    * @param size 初始化大小
    * @param maxSize 最大容量大小
    */
  def init(poolName: String, driver: String, url: String, username: String, password: String, size: Int, maxSize: Int): Unit = {
    if (!ConnectionPool.isInitialized(Symbol(poolName))) {
      Class.forName(driver)
      val settings = ConnectionPoolSettings(
        initialSize = size,
        maxSize = maxSize,
        connectionTimeoutMillis = 3000L,
        validationQuery = "select 1")
      ConnectionPool.add(Symbol(poolName), url, username, password, settings)
      info(s"Initialize connection pool：$poolName")

    }
  }

  def usingDB[A](poolName: String)(execute: DB => A): A =
    using(DB(ConnectionPool(Symbol(poolName)).borrow()))(execute)
}
