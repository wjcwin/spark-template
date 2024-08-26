package com.wjc.utils

import com.alibaba.fastjson.JSONObject
import com.wjc.core.Logging
import scalaj.http.Http

/**
 * <p></p>
 *
 * @author 王建成 2020/11/24 14:18
 * @version V1.0
 * @modify by user: 王建成 2020/11/24
 * @modify by reason:{原因}
 **/
object WxMonitor extends Logging{
  //企业微信/钉钉群机器人 url
  val url: String =ConfigsUtil.webhook

  def pushMessage(message:String): Unit ={
    val data=new JSONObject()
    data.put("msgtype","markdown")
    val markdown = new JSONObject()
    markdown.put("content",message)
    data.put("markdown",markdown)
    Http(url).postData(data.toJSONString).header("Content-Type", "application/json").asString
  }

  def pushMessage(message:String,robotUrl:String): Unit ={
    val data=new JSONObject()
    data.put("msgtype","markdown")
    val markdown = new JSONObject()
    markdown.put("content",message)
    data.put("markdown",markdown)
    Http(robotUrl).postData(data.toJSONString).header("Content-Type", "application/json").asString
  }

  def pushMessageTextUrl(message:String,robotUrl:String): Unit ={
    val data=new JSONObject()
    data.put("msgtype","text")
    val markdown = new JSONObject()
    markdown.put("content",message)
    data.put("text",markdown)
    Http(robotUrl).postData(data.toJSONString).header("Content-Type", "application/json").asString
  }



}
