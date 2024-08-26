package hudi14

import com.wjc.client.HDFSClient
import com.wjc.core.{Hudiing, Sparking}
import org.apache.hadoop.fs.Path
import org.scalatest.funsuite.AnyFunSuite

import scala.io.Source

/**
 * @author: 王建成
 * @since: 2023/10/17 16:51
 * @description: 描述
 */
class Test1 extends AnyFunSuite with Sparking with Hudiing {
  enableLocalSupport()
  setCosConf()
  setSocksProxy()


  test("1") {
    val spark = getSparkSession(None)
    // 屏蔽不必要的日志 ,在终端上显示需要的日志
    setLogLevel("info")

    import com.qcloud.chdfs.fs.CHDFSHadoopFileSystemJarLoader
    import com.qcloud.chdfs.fs.CHDFSHadoopFileSystemAdapter
    //    import com.qcloud.cos.http.HandlerAfterProcess
    import org.apache.hadoop.fs.CosNativeFileSystemStore
    import org.apache.hadoop.fs.CosFileSystem
    import com.qcloud.cos.auth.InstanceCredentialsUtils
    import org.apache.hadoop.net.SocketIOWithTimeout
    import org.apache.spark.sql.hive.HiveUtils
    val basePath = "cosn://hdfs-1309102445/user/hudi"
    val tablePath = basePath + "/superpark/bus_order"
    val hadoopConf = spark.sparkContext.hadoopConfiguration
    println(hadoopConf.get("fs.cosn.bucket.region"))
    val client = HDFSClient("cosn://hdfs-1309102445", hadoopConf)
    val ls = client.listStatus(new Path(s"$tablePath"))

    println(ls.mkString("\n"))

  }

  test("2") {
    val spark = getSparkSession(None)
    val df = getHudi(spark, "superpark.bus_order", Set("202005"))
    df.printSchema()

    df.show(false)

  }

  test("3") {
    val source = Source.fromFile("D:\\tmp\\conf\\cos.txt")
    source.getLines().foreach(line => {
      val kvs = line.split("=")
      println(s"key:${kvs(0)},value:${kvs(1)}")
    })
    source.close()
  }

  def setCosConf(): Unit = {
    val source = Source.fromFile("D:\\tmp\\conf\\cos.txt")
    source.getLines().foreach(line => {
      val kvs = line.split("=")
      conf.set(kvs(0), kvs(1))
    })
    source.close()
    conf.set("spark.hadoop.fs.AbstractFileSystem.cosn.impl", "org.apache.hadoop.fs.CosN")
    conf.set("spark.hadoop.fs.cosn.bucket.region", "ap-shanghai")
    conf.set("spark.hadoop.fs.cosn.impl", "org.apache.hadoop.fs.CosFileSystem")
  }

  def setSocksProxy() {
    System.setProperty("socksProxyHost", "localhost")
    System.setProperty("socksProxyPort", "8083")
    System.setProperty("HADOOP_USER_NAME", "hadoop")
  }

}
