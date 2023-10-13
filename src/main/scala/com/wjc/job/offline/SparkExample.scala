package com.wjc.job.offline

import com.wjc.core.SparkJob
import org.apache.spark.sql.functions._

object SparkExample extends SparkJob {
  //调用sparking中的各种启用插件的方法 需要在这个地方调用,或者在 getSparkSession()之前调用
  enableLocalSupport()
//  setLogLevel("INFO")

  /**
   * 子类必须实现handle方法，在该方法内实现业务代码
   */
  override def handle(args: Array[String]): Unit = {
    val spark = getSparkSession(None)
    import spark.implicits._
    val df = spark.sparkContext.parallelize(Seq((1, "a"), (2, "b"))).toDF("id", "value")

    val result = df.groupBy("id")
      .agg(
        max($"value").as("max_value"),
        min($"value").as("min_value"),
        count(lit(1)).as("count")
      )

    result.show(false)

    spark.stop()
  }
}
