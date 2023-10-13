package com.wjc.job.offline


import com.wjc.client.HDFSClient
import com.wjc.core.{HdfsConf, Logging, Sparking}
import com.wjc.utils.ConfigsUtil
import org.apache.hadoop.fs._
import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}

import scala.collection.mutable

/**
  * @program: com.hikcreate.job.cleanjob->MergeSmallFile
  * @description: 合并小文件
  * @author: wangjiancheng
  * @create: 2020/7/7 16:41
  **/
//noinspection ScalaDocUnknownTag
object MergeSmallFile extends HdfsConf with Sparking with Logging {
  val hdfs: FileSystem =HDFSClient(ConfigsUtil.HDFS_URL,hdfsConf).fs
  private var dirs = ""
  private val tmpDir = "/hive/MergeSmallFile_tmp"


  /**
    *
    * @param args 输入参数：根目录
    */
  def run(args: Array[String]): Unit = {
    assert(args != null && args.nonEmpty, "输入参数不能为空，请指定要进行合并小文件的hdfs根目录")
    if (args(0) != null && args(0).trim.nonEmpty) {
      dirs = args(0)
    }
    val spark: SparkSession = SparkSession.builder().config(conf).getOrCreate()
    val dirToNum: mutable.Map[String, Int] = takePartition()
    dirToNum.foreach((dirAndNum: (String, Int)) => {
      try {
        val frame: DataFrame = spark.read.text(dirAndNum._1)
        warn(s"合并目录[${dirAndNum._1}]下的小文件为[${dirAndNum._2}]个文件到临时目录[$tmpDir]")
        frame.sqlContext.sparkContext.hadoopConfiguration.set("mapred.output.compress", "false") //去除压缩
        frame.repartition(dirAndNum._2).write.mode(SaveMode.Overwrite).text(tmpDir)
        warn(s"将临时目录[$tmpDir ]下的文件移动到源目录中 [${dirAndNum._1}]")
        mv(tmpDir, dirAndNum._1)
        warn(s"将源目录中[${dirAndNum._1}]的小文件进行删除")
        del(dirAndNum._1)
      } catch {
        case e =>
          warn(s"合并目录[${dirAndNum._1}]下的小文件出错 ${e.getMessage}")
      }
    })
    spark.stop()
  }

  /** 根据根目录获取指定下钻层级叶子目录下  map(""叶子目录->"叶子目录小文件合并后的个数") */
  def takePartition(): scala.collection.mutable.Map[String, Int] = {
    var map: mutable.Map[String, Int] = scala.collection.mutable.Map[String, Int]()
    val directorys: Array[String] = dirs.split(",")
    directorys.foreach((dir: String) => {
      val path = new Path(dir)
      if (hdfs.exists(path)) {
        val listfile: Array[FileStatus] = hdfs.listStatus(path)
        if (isBottomDir(listfile)) {
          val currentSize: Int = hdfs.listStatus(path).length //目标目录下文件个数
          val srcSize: Int = takePartition(path) //计算合并后的文件个数
          if (currentSize > srcSize) map(path.toString) = srcSize //当前文件个数小于 计算个数时才需要进行小文件合并
        } else {
          map = map ++ recuersionFile(listfile)
        }
      }
    })

    map
  }

  /** 递归遍历 ，获取到最底层文件目录， */
  def recuersionFile(listFile: Array[FileStatus]): scala.collection.mutable.Map[String, Int] = {
    var map: mutable.Map[String, Int] = scala.collection.mutable.Map[String, Int]()
    listFile.foreach((file: FileStatus) =>
      if (file.isDirectory) {
        val path: Path = file.getPath
        val statuses: Array[FileStatus] = hdfs.listStatus(path)
        if (isBottomDir(statuses)) {
          val currentSize: Int = statuses.length //目标目录下文件个数
          val srcSize: Int = takePartition(path) //计算合并后的文件个数
          if (currentSize > srcSize) map(path.toString) = srcSize //当前文件个数小于 计算个数时才需要进行小文件合并
        } else {
          map = map ++ recuersionFile(statuses)
        }
      })
    map
  }

  /** 判断当前是否为最底层目录 */
  def isBottomDir(listFile: Array[FileStatus]): Boolean = {
    var dirNum = 0
    var fileNum = 0
    listFile.foreach((file: FileStatus) =>
      if (file.isDirectory) {
        dirNum = dirNum + 1
      } else {
        fileNum = fileNum + 1
      })
    fileNum > dirNum
  }


  /** 根据根目录获取第二层叶子目录下  map(""叶子目录->"叶子目录小文件合并后的个数") */
  /* def takePartition(): scala.collection.mutable.Map[String, Int] = {
     val map: mutable.Map[String, Int] = scala.collection.mutable.Map[String, Int]()
     val listfile: Array[FileStatus] = hdfs.listStatus(new Path(dir))
     listfile.foreach((file: FileStatus) => //表目录
       if (file.isDirectory) {
         val childFiles: Array[FileStatus] = hdfs.listStatus(file.getPath) //分区目录
         childFiles.foreach((childfile: FileStatus) => {
           val currentSize: Int = hdfs.listStatus(childfile.getPath).length //分区目录下文件个数
           val srcSize: Int = takePartition(childfile.getPath) //计算合并后的文件个数
           if (currentSize > srcSize) map(childfile.getPath.toString) = srcSize //当前文件个数小于 计算个数时才需要进行小文件合并
         })
       })
     map
   } */

  /** 根据输入目录计算目录大小，并以128*2M大小计算partition */
  def takePartition(src: String): Int = {
    val cos: ContentSummary = hdfs.getContentSummary(new Path(src))
    val sizeM: Long = cos.getLength / 1024 / 1024
    val partNum: Int = sizeM / 128 match {
      case 0 => 1
      case _ => (sizeM / 128).toInt
    }
    partNum
  }

  /** 根据输入目录计算目录大小，并以128*2M大小计算partition */
  def takePartition(src: Path): Int = {
    val cos: ContentSummary = hdfs.getContentSummary(src)
    val sizeM: Long = cos.getLength / 1024 / 1024
    val partNum: Int = sizeM / 128 match {
      case 0 => 1
      case _ => (sizeM / 128).toInt
    }
    partNum
  }

  /** 将临时目录中的结果文件mv到源目录，并以tocc-为文件前缀 */
  def mv(fromDir: String, toDir: String): Unit = {
    val srcFiles: Array[Path] = FileUtil.stat2Paths(hdfs.listStatus(new Path(fromDir)))
    for (p: Path <- srcFiles) {
      // 如果是以part开头的文件则修改名称
      if (p.getName.startsWith("part")) {
        hdfs.rename(p, new Path(toDir + "/tocc-" + p.getName))
      }
    }
  }

  /** 删除原始小文件 */
  def del(fromDir: String): Unit = {
    val files: Array[Path] = FileUtil.stat2Paths(hdfs.listStatus(new Path(fromDir), new FileFilter()))
    for (f: Path <- files) {
      hdfs.delete(f, true) // 迭代删除文件或目录
    }
  }
}

class FileFilter extends PathFilter {
  @Override def accept(path: Path): Boolean = {
    !path.getName.startsWith("tocc-")
  }
}


