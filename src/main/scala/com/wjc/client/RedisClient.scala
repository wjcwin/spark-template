package com.wjc.client

import com.wjc.core.{Borrow, Logging}
import redis.clients.jedis.{Jedis, JedisPool, JedisPoolConfig}

class RedisClient(func:() => JedisPool) extends Borrow with Serializable {

  lazy val pool: JedisPool = func()

  def usingRedis[A](execute:Jedis=>A):A = using(pool.getResource)(execute)
}

object RedisClient extends Logging {

  def apply(host:String,port:Int,password:String,timeout:Int = 1000*2): RedisClient = {
    val func = () => {
      val config = new JedisPoolConfig
      val pool = new JedisPool(config,host,port,timeout,password)
      sys.addShutdownHook{
        warn("Execute hook thread: RedisClient")
        pool.destroy()
      }
      pool
    }
    new RedisClient(func)
  }
}
