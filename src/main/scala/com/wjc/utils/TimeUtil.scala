package com.wjc.utils

import java.text.SimpleDateFormat

import org.joda.time.{DateTime, Duration}
import org.joda.time.format.DateTimeFormat

/**
  * @program: com.wjc.utils->TimeUtil
  * @description: 描述
  * @author: wangjiancheng
  * @create: 2020/9/2 10:24
  **/
//noinspection ScalaDocUnknownTag
object TimeUtil {

  /**
    * 判断字符串是否为指定格式日期
    *
    * @param str    日期字符串
    * @param format 指定日期格式
    * @return
    */
  def isDate(str: String, format: String): Boolean = {
    if (str == null && str.isEmpty) {
      false
    } else {
      val dataFormat: SimpleDateFormat = new SimpleDateFormat(format)
      try {
        dataFormat.parse(str)
        true
      } catch {
        case _: Throwable =>
          false
      }
    }
  }

  /**
    * 两个时间差 yyyy-MM-dd HH:mm:ss格式
    *
    * @param a 开始时间字符串
    * @param b 结束时间字符串
    * @return 秒数
    */
  def getBetweenSecond(a: String, b: String): Long = {
    val startDate: DateTime = DateTime.parse(a, DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss"))
    val endDate: DateTime = DateTime.parse(b, DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss"))
    Math.abs(new Duration(startDate, endDate).getStandardSeconds)
  }
}
