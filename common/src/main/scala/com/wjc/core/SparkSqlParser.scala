package com.wjc.core

import com.wjc.model.TabLine
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.catalyst.analysis.UnresolvedRelation
import org.apache.spark.sql.catalyst.plans.logical._
import org.apache.spark.sql.execution.command.DropTableCommand
import org.apache.spark.sql.execution.datasources.CreateTable

import scala.collection.mutable

class SparkSqlParser extends Logging {

  def resolveLogicPlan(sparkSession: SparkSession, plan: LogicalPlan): (mutable.Set[TabLine],
    mutable.Set[TabLine],
    mutable.Set[TabLine]) = {
    val inputTables = mutable.Set[TabLine]()
    val outputTables = mutable.Set[TabLine]()
    val deleteTables = mutable.Set[TabLine]()
    resolveLogic(sparkSession, plan, inputTables, outputTables, deleteTables)
    (inputTables, outputTables, deleteTables)
  }

  //递归解析
  def resolveLogic(sparkSession: SparkSession,
                   plan: LogicalPlan,
                   inputTables: mutable.Set[TabLine],
                   outputTables: mutable.Set[TabLine],
                   deleteTables: mutable.Set[TabLine]): Unit = {
    plan match {
      case plan: Project =>
        resolveLogic(sparkSession, plan.child, inputTables, outputTables, deleteTables)
      case plan: UnresolvedRelation =>
        val url = sparkSession.conf.get("hive.metastore.uris")
        val database = plan.tableIdentifier.database.getOrElse("")
        val table = plan.tableIdentifier.table
        val tabLine = TabLine(url, Datasource.hive, database, table)
        inputTables.add(tabLine)
      case plan: CreateTable => //create table语句
        if(plan.query.isDefined) {
          resolveLogic(sparkSession, plan.query.get, inputTables, outputTables, deleteTables)
        }
        val tableDesc = plan.tableDesc
        val datasource = tableDesc.provider.getOrElse("") //数据源类型
        if(datasource.equals(Datasource.hive)){
          val url = sparkSession.conf.get("hive.metastore.uris")
          val database = tableDesc.identifier.database.getOrElse("")
          val table = tableDesc.identifier.table
          val tabLine = TabLine(url, datasource, database, table)
          outputTables.add(tabLine)
        }
      case plan: DropTableCommand => //drop table语句
        val tableName = plan.tableName
        if(plan.isView){
          //
        } else {
          val url = sparkSession.conf.get("hive.metastore.uris")
          val datasource = Datasource.hive
          val database = tableName.database.getOrElse("")
          val table = tableName.table
          val tabLine = TabLine(url, datasource, database, table)
          deleteTables.add(tabLine)
        }
      case plan: InsertIntoTable => // insert into/overwrite语句
        resolveLogic(sparkSession, plan.table, outputTables, inputTables, deleteTables)
        resolveLogic(sparkSession, plan.query, inputTables, outputTables, deleteTables)
      //全局限制，最多返回 limitExpr 对应条 records。
      case plan: GlobalLimit =>
        resolveLogic(sparkSession, plan.child, inputTables, outputTables, deleteTables)
      //分区级限制（非全局），限制每个物理分区最多返回 limitExpr 对应条 records。
      case plan: LocalLimit =>
        resolveLogic(sparkSession, plan.child, inputTables, outputTables, deleteTables)
      case `plan` => info("******child plan******:\n"+plan)
    }
  }
}