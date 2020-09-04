package com.wjc.model

case class TabLine(url:String,
                   datasource:String,
                   database:String,
                   table:String) {

  override def toString: String = {
    s"""|TabLine[
        |url=$url,
        |datasource=$datasource,
        |database=$database,
        |table=$table]
        |""".stripMargin
  }
}
