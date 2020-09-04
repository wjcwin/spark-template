package com.wjc.utils

/**
  * @program: com.wjc.utils->Tools
  * @description: 描述
  * @author: wangjiancheng
  * @create: 2020/9/2 9:59
  **/
//noinspection ScalaDocUnknownTag
object Tools {

  /**
    * 验证身份证号是否合法
    * @param IDNumber 待验证身份证
    * @return Boolean
    */
  def isIDNumber(IDNumber: String): Boolean = {
    if (IDNumber == null || "" == IDNumber) return false
    // 定义判别用户身份证号的正则表达式（15位或者18位，最后一位可以为字母）
    val regularExpression = "(^[1-9]\\d{5}(18|19|20)\\d{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$)|" + "(^[1-9]\\d{5}\\d{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)\\d{3}$)"
    val matches = IDNumber.matches(regularExpression)
    //判断第18位校验值
    if (matches) if (IDNumber.length == 18) try {
      val charArray = IDNumber.toCharArray
      //前十七位加权因子
      val idCardWi = Array(7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2)
      //这是除以11后，可能产生的11位余数对应的验证码
      val idCardY = Array("1", "0", "X", "9", "8", "7", "6", "5", "4", "3", "2")
      var sum = 0
      var i = 0
      while ( {
        i < idCardWi.length
      }) {
        val current = String.valueOf(charArray(i)).toInt
        val count = current * idCardWi(i)
        sum += count

        {
          i += 1;
          i - 1
        }
      }
      val idCardLast = charArray(17)
      val idCardMod = sum % 11
      if (idCardY(idCardMod).toUpperCase == String.valueOf(idCardLast).toUpperCase) return true
      else {
        return false
      }
    } catch {
      case e: Exception =>
        e.printStackTrace()
        return false
    }
    matches
  }



}
