package com.wjc.core

import org.apache.hadoop.conf.Configuration

/**
  * @program: com.wjc.core->HdfsConf
  * @description: 描述
  * @author: wangjiancheng
  * @create: 2020/9/1 18:37
  **/
//noinspection ScalaDocUnknownTag
trait HdfsConf {
  //以nameservices 的方式初始化 FileSystem（HDFS HA）
  val hdfsConf = new Configuration()
  hdfsConf.set("fs.defaultFS", "hdfs://hikbigdata")
  hdfsConf.set("dfs.nameservices", "bigdata")
  hdfsConf.set("dfs.ha.namenodes.nameservices", "nn1,nn2")
  hdfsConf.set("dfs.namenode.rpc-address.nameservices.nn1", "10.197.236.211:8020")
  hdfsConf.set("dfs.namenode.rpc-address.nameservices.nn2", "10.197.236.212:8020")
  hdfsConf.set("dfs.client.failover.proxy.provider.chkjbigdata", "org.apache.hadoop.hdfs.server.namenode.ha.ConfiguredFailoverProxyProvider")
  hdfsConf.setBoolean("dfs.support.append", true)
}
