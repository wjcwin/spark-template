package com.wjc.utils

import java.sql.{Connection, DatabaseMetaData, DriverManager, ResultSet}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.util.control.Exception.ignoring

/**
  * @program: com.wjc.utils->HiveUtil
  * @description: 描述
  * @author: wangjiancheng
  * @create: 2020/9/2 10:09
  **/
//noinspection ScalaDocUnknownTag
object HiveUtil {

  /**
    * 获取hive表的字段
    * @param database   hive库
    * @param tableNames hive表
    * @return
    */
  def getFieldsByTable(database: String, tableNames: ArrayBuffer[String]): mutable.HashMap[String, ArrayBuffer[String]] = {
    var tableColMap = scala.collection.mutable.HashMap("" -> new ArrayBuffer[String]())
    Class.forName(Apollos.hiveDriver)
    val connection: Connection = DriverManager.getConnection(
      Apollos.hiveURL,
      Apollos.hiveUserName,
      Apollos.hivePassWord)
    try {
      val metaData: DatabaseMetaData = connection.getMetaData
      if (database != null && database.trim.nonEmpty && tableNames != null && tableNames.nonEmpty) {
        tableNames.foreach { tableName: String =>
          val rs: ResultSet = metaData.getColumns(null, database, tableName, "%")
          val cols = new ArrayBuffer[String]()
          while (rs.next()) {
            val column: String = rs.getString("column_name")
            cols += column
          }
          tableColMap += (tableName -> cols)
        }
      }
    } finally {
      ignoring(classOf[Throwable]) apply connection.close()
    }

    tableColMap
  }

}
