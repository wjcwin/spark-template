package com.wjc.client

import com.wjc.core.{Borrow, Logging}
import org.redisson.Redisson
import org.redisson.api.RedissonClient
import org.redisson.config.{Config, ReadMode}

/**
 * <p>
 * redisson 客户端
 * </p>
 *
 * @author 王建成 2022/7/8 18:07
 **/
class RedissonBucketClient(func: () => RedissonClient) extends Borrow {
  lazy val redi: RedissonClient = func()
}

object RedissonBucketClient extends Logging {

  def apply(nodes: String, masterName: String, password: String, masterPoolSize: Int = 2, slavePoolSize: Int = 2): RedissonBucketClient = {

    val func = () => {
      val config = new Config()
      val nodeStr = nodes.split(",")
      val newNodes = nodeStr.map(index => {
        if (index.startsWith("redis://")) {
          index
        } else {
          "redis://" + index
        }
      })

      val serverConfig = config.useSentinelServers
        .addSentinelAddress(newNodes: _*)
        .setMasterName(masterName)
        .setDatabase(0)
        .setMasterConnectionMinimumIdleSize(1)
        .setMasterConnectionPoolSize(masterPoolSize)
        .setSlaveConnectionMinimumIdleSize(1)
        .setSlaveConnectionPoolSize(slavePoolSize)
        .setReadMode(ReadMode.SLAVE)
        .setPingConnectionInterval(300000)
        .setTimeout(300000)

      serverConfig.setPassword(password)
      Redisson.create(config)
    }
    val client = new RedissonBucketClient(func)
    sys.addShutdownHook {
      warn("Execute hook thread: RedissonBucketClient")
      client.redi.shutdown()
    }
    client

  }

}
