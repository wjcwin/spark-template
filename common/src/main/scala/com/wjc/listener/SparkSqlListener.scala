package com.wjc.listener

import com.wjc.client.Neo4jClient
import com.wjc.core.{Borrow, Logging, SparkSqlParser}
import com.wjc.model.TabLine
import org.apache.spark.sql.execution.QueryExecution
import org.apache.spark.sql.util.QueryExecutionListener
import org.neo4j.driver.Transaction

/**
 * 每条sql执行完毕后都会调用这个监听器
 */
class SparkSqlListener extends QueryExecutionListener with Borrow with Logging {

  override def onSuccess(funcName: String, qe: QueryExecution, durationNs: Long): Unit = {
    val sparkSqlParser = new SparkSqlParser()
    val output = sparkSqlParser.resolveLogicPlan(qe.sparkSession, qe.logical)
    val inputTables = output._1
    val outputTables = output._2
    val deleteTables = output._3
    println("---inputTables---")
    inputTables.foreach(x=>println(x.toString))
    println("---outputTables---")
    outputTables.foreach(x=>println(x.toString))
    println("---deleteTables---")
    deleteTables.foreach(x=>println(x.toString))
    val neo4j = Neo4jClient("bolt://10.197.236.211:7687", "neo4j", "123456")
    neo4j.useSession{ session =>
      using(session.beginTransaction()){tx =>
        inputTables.foreach{inputTable => insertVertex(tx, inputTable)}
        outputTables.foreach{outputTable => insertVertex(tx, outputTable)}
        deleteTables.foreach(deleteTable => deleteVertex(tx, deleteTable))
        if(inputTables.nonEmpty){
          inputTables.foreach{origin =>
            outputTables.foreach{dest =>
              insertEdge(tx, origin, dest)
            }
          }
        }
        tx.commit()
      }
    }
    neo4j.close()
  }

  def insertVertex(tx: Transaction, tabLine: TabLine): Unit = {
    val data = tx.run(s"""|MATCH (a:Table) WHERE
                          |a.url = '${tabLine.url}' and
                          |a.datasource = '${tabLine.datasource}'and
                          |a.database = '${tabLine.database}'and
                          |a.name = '${tabLine.table}'
                          |RETURN a
                          |""".stripMargin.replaceAll("\r\n"," "))
    if(!data.hasNext) {
      tx.run(s"""|CREATE (a:Table {
                 |url: '${tabLine.url}',
                 |datasource: '${tabLine.datasource}',
                 |database: '${tabLine.database}',
                 |name: '${tabLine.table}'
                 |})
                 |""".stripMargin)
    }
  }

  def insertEdge(tx: Transaction, origin: TabLine, dest: TabLine): Unit = {
    val data = tx.run(s"""|MATCH (a:Table)-[r:TableBlood]->(b:Table) WHERE
                          |a.name = '${origin.table}' AND b.name = '${dest.table}' and
                          |a.url = '${origin.url}' AND b.url = '${dest.url}' and
                          |a.datasource = '${origin.datasource}' and b.datasource = '${dest.datasource}' and
                          |a.database = '${origin.database}' and b.database = '${dest.database}'
                          |RETURN r
                          |""".stripMargin.replaceAll("\r\n"," "))
    if(!data.hasNext){
      tx.run(s"""|MATCH (a:Table),(b:Table) WHERE
                 |a.name = '${origin.table}' AND b.name = '${dest.table}' and
                 |a.url = '${origin.url}' AND b.name = '${dest.url}' and
                 |a.datasource = '${origin.datasource}' and b.datasource = '${dest.datasource}' and
                 |a.database = '${origin.database}' and b.database = '${dest.database}'
                 |CREATE (a)-[r:TableBlood]->(b)
                 |""".stripMargin.replaceAll("\r\n"," "))
    }
  }

  def deleteVertex(tx: Transaction, tabLine: TabLine): Unit = {
    tx.run(s"""|MATCH (a:Table) WHERE
               |a.url = '${tabLine.url}' and
               |a.datasource = '${tabLine.datasource}' and
               |a.database = '${tabLine.database}' and
               |a.name = '${tabLine.table}'
               |DETACH DELETE a
               |""".stripMargin)
  }

  override def onFailure(funcName: String, qe: QueryExecution, exception: Exception): Unit = {}
}
