package com.wjc.job.offline

import com.wjc.core.Sparking


object SparkExample extends Sparking {

  def run(): Unit = {

    enableLocalSupport()

    //hive.metastore.uris
    val spark = getSparkSession(Some("thrift://hadoop01:9083"))
    spark.sql("select * from default.liuxinpei1").show(20, truncate = false)

  }
}
