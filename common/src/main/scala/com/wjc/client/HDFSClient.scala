package com.wjc.client

import java.io.ByteArrayInputStream
import java.net.URI

import com.wjc.core.{Borrow, Logging}
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileStatus, FileSystem, Path}
import org.apache.hadoop.io.IOUtils
import org.apache.spark.SparkContext
import org.joda.time.format.DateTimeFormat
import org.joda.time.{DateTime, Days}

class HDFSClient(func: () => FileSystem) extends Borrow with Logging with Serializable {

  lazy val fs: FileSystem = func()

  def createFile(path: String): Boolean = fs.createNewFile(new Path(path))

  //删除该路径上的文件，true 如果该文件是一个目录的话，则递归删除该目录
  def deleteFile(path: String, boolean: Boolean): Boolean = fs.delete(new Path(path), boolean)

  def append(content: String, path: String): Unit = {
    val in = new ByteArrayInputStream((content + "\n").getBytes("UTF-8"))
    val out = fs.append(new Path(path))
    IOUtils.copyBytes(in, out, 4096, true)
  }

  //可以重复使用一个连接，减少每次获取对象建立连接的时间
  def batchAppend(path: String, content: List[String]): Unit = {
    val out = fs.append(new Path(path))
    using(out) { out =>
      content.foreach { line =>
        val in = new ByteArrayInputStream((line + "\n").getBytes("UTF-8"))
        using(in) { in =>
          IOUtils.copyBytes(in, out, 4096, false)
        }
      }
    }
  }

  def getTimePath(sc: SparkContext, rootPath: String, start: String, interval: Int): String = {
    val startTime: DateTime = DateTime.parse(start, DateTimeFormat.forPattern("yyyyMMdd"))
    val endTime: DateTime = startTime.plusDays(interval)
    (0 until Days.daysBetween(startTime, endTime).getDays).map { x =>
      val secondDire: DateTime = startTime.plusDays(x)
      rootPath + "/" + secondDire.toString("yyyyMMdd") + ".log"
    }.filter(x => FileSystem.get(sc.hadoopConfiguration).exists(new Path(x))).mkString(",")
  }

  def exists(path:String):Boolean={
    fs.exists(new Path(path))
  }

  def exists(path:Path):Boolean={
    fs.exists(path)
  }

  def listStatus(path:Path): Array[FileStatus] ={
    fs.listStatus(path)
  }
}

object HDFSClient extends Logging {

  def apply(url: String, conf: Configuration): HDFSClient = {
    val func = () => {
      val fs = FileSystem.get(URI.create(url), conf)
      sys.addShutdownHook {
        warn("Execute hook thread: HDFSSink")
        fs.close()
      }
      fs
    }
    new HDFSClient(func)
  }

  def main(args: Array[String]): Unit = {

    val conf = new Configuration()
    //以nameservices 的方式初始化 FileSystem（HDFS HA）
    conf.set("fs.defaultFS", "hdfs://hikbigdata")
    conf.set("dfs.nameservices", "bigdata")
    conf.set("dfs.ha.namenodes.nameservices", "nn1,nn2")
    conf.set("dfs.namenode.rpc-address.nameservices.nn1", "10.197.236.211:8020")
    conf.set("dfs.namenode.rpc-address.nameservices.nn2", "10.197.236.212:8020")
    conf.set("dfs.client.failover.proxy.provider.chkjbigdata", "org.apache.hadoop.hdfs.server.namenode.ha.ConfiguredFailoverProxyProvider")
    conf.setBoolean("dfs.support.append", true)
    val sink = HDFSClient("hdfs://10.197.236.211:8020", conf)
    sink.deleteFile("/test/a", boolean = true)
  }
}
