package com.wjc.core

import org.slf4j.{Logger, LoggerFactory}

trait Logging {

  private def log: Logger = LoggerFactory.getLogger(this.getClass.getName.stripSuffix("$"))

  protected def debug(msg: => String): Unit = if (log.isDebugEnabled) log.debug(msg)

  protected def info(msg: => String): Unit = if (log.isInfoEnabled) log.info(msg)

  protected def warn(msg: => String): Unit = if (log.isWarnEnabled) log.warn(msg)

  protected def error(msg: => String): Unit = if (log.isErrorEnabled) log.error(msg)
}
