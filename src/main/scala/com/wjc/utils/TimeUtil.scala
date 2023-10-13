package com.wjc.utils

import java.text.SimpleDateFormat

import org.joda.time.{DateTime, Duration, Interval}
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

  def getDateInterVal(start: DateTime, end: DateTime): String = {
    val interval = new Interval(start.getMillis, end.getMillis).toPeriod
    if(interval.getDays>0){
      s"${interval.getDays}天 ${interval.getHours}小时 ${interval.getMinutes}分钟${interval.getSeconds}秒${interval.getMillis}毫秒"
    }else if (interval.getHours>0){
      s"${interval.getHours}小时 ${interval.getMinutes}分钟${interval.getSeconds}秒${interval.getMillis}毫秒"
    }else if (interval.getMinutes>0){
      s"${interval.getMinutes}分钟${interval.getSeconds}秒${interval.getMillis}毫秒"
    }else if (interval.getSeconds>0){
      s"${interval.getSeconds}秒${interval.getMillis}毫秒"
    }else{
      s"${interval.getMillis}毫秒"
    }

  }
}
