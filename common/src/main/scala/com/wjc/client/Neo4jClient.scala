package com.wjc.client

import com.wjc.core.{Borrow, Logging}
import org.neo4j.driver.{AuthTokens, Driver, GraphDatabase, Session}

/**
 * http://10.197.236.211:7474
 */
class Neo4jClient(func:() => Driver) extends Borrow {

  lazy val driver: Driver = func()

  def useSession[A](execute: Session=> A): Unit = using(driver.session())(execute)

  //删除某个标签下的所有节点(提前删除索引)
  def removeLabel(label: String): Unit = useSession(session=> session.run(s"MATCH (r:${label}) DETACH DELETE r"))

  //移除节点
  def removeNode(): Unit = {}

  //查找节点的关系

  //移除节点间的关系
  def removeRelationship(): Unit = {}

  def close(): Unit = driver.close()
}

object Neo4jClient extends Logging {

  def apply(url:String, username:String, password:String): Neo4jClient = {
    val func = () => GraphDatabase.driver(url, AuthTokens.basic(username, password))
    new Neo4jClient(func)
  }

  def main(args: Array[String]): Unit = {
    val neo4j = Neo4jClient("bolt://10.197.236.211:7687", "neo4j", "123456")
    //CREATE CONSTRAINT ON (a:Table) ASSERT a.name IS UNIQUE
    //DROP CONSTRAINT ON (a:Table) ASSERT a.name IS UNIQUE
    //neo4j.removeLabel("Table")
    neo4j.useSession{ session=>
      val res = session.run(
        """
          |MATCH (n:Table)-[r:TableBlood*]->(m:Table)
          |WHERE m.name = 'liuxinpei3'
          |RETURN n as source,m as target,r as relation_list
          |""".stripMargin)
      println(res.next().asMap())
    }
    neo4j.close()
  }
}
