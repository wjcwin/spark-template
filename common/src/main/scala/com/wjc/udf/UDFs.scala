package com.wjc.udf
import org.apache.spark.sql.expressions.UserDefinedFunction
import org.apache.spark.sql.functions._
/**
  * @program: com.wjc.udf->UDFs
  * @description: 描述
  * @author: wangjiancheng
  * @create: 2020/9/2 14:39
  **/
//noinspection ScalaDocUnknownTag
object UDFs {
  def len(str: String): Int = str.length

  def ageThan(age: Int, small: Int): Boolean = age > small

  val ageThaner: UserDefinedFunction = udf((age: Int, bigger: Int) => age < bigger)
}
