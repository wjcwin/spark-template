package com.wjc.utils

import java.util.UUID

/**
  * @program: com.wjc.utils->UUIDUtil
  * @description: 描述
  * @author: wangjiancheng
  * @create: 2020/9/2 10:17
  **/
//noinspection ScalaDocUnknownTag
object UUIDUtil {

  /**
    * 创建 36位随机UUID
    * @return
    */
  def createUUID(): String = {
    UUID.randomUUID().toString
  }

  /**
    * 根据指定字符创建 36位UUID
    * @param str 输入字符串
    * @return
    */
  def createUUID(str: String): String = {
    UUID.nameUUIDFromBytes(str.getBytes()).toString
  }

  /**
    * 创建32位的随机UUID
    * @return
    */
  def createUUID32(): String = {
    UUID.randomUUID().toString.replace("-", "")
  }
  /**
    * 根据指定字符创建32位的UUID
    * @return
    */
  def createUUID32(str: String): String = {
    UUID.nameUUIDFromBytes(str.getBytes()).toString.replace("-", "")
  }
}
