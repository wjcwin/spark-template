package com.wjc.utils

import java.awt.geom.Point2D

/**
  * @program: com.wjc.utils->GPSUtil
  * @description: 描述
  * @author: wangjiancheng
  * @create: 2020/9/2 10:15
  **/
//noinspection ScalaDocUnknownTag
object GPSUtil {


  /**
    * 角度换算成弧度
    *
    * @param d 角度
    * @return
    */
  def rad(d: Double): Double = {
    d * Math.PI / 180.0
  }

  /**
    * 根据经纬度计算两点间的球面距离
    *
    * @param lat1 纬度1
    * @param lng1 经度1
    * @param lat2 纬度2
    * @param lng2 经度2
    * @return
    */
  def Distance(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double = {
    var s = 0D
    if (null != lat1 && null != lng1 && null != lat2 && null != lng2) {
      val EARTH_RADIUS = 6371.830D
      val radLat1: Double = rad(lat1)
      val radLat2: Double = rad(lat2)
      val radLng1: Double = rad(lng1)
      val radLng2: Double = rad(lng2)
      //var s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) + Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)))
      s = Math.acos(Math.cos(radLat1) * Math.cos(radLat2) * Math.cos(radLng1 - radLng2) + Math.sin(radLat1) * Math.sin(radLat2))
      s = s * EARTH_RADIUS
      s = Math.round(s * 1000D) / 1000D
      s
    } else {
      0D
    }
  }

  val x_PI: Double = 3.14159265358979324 * 3000.0 / 180.0
  val PI = 3.14159265358979324
  val a = 6378245.0
  //克拉索夫斯基椭球参数长半轴a
  val ee = 0.00669342162296594323 //克拉索夫斯基椭球参数第一偏心率平方

  /**
    * 火星坐标系 (GCJ-02) 与百度坐标系 (BD-09) 的转换 即谷歌、高德 转 百度
    *
    * @param lng 火星坐标系经度
    * @param lat 火星坐标系纬度
    * @return 百度坐标系经纬度
    */
  def gcj02tobd09(lng: Double, lat: Double): (Double, Double) = {
    val z: Double = Math.sqrt(lng * lng + lat * lat) + 0.00002 * Math.sin(lat * x_PI)
    val theta: Double = Math.atan2(lat, lng) + 0.000003 * Math.cos(lng * x_PI)
    val bd_lng: Double = z * Math.cos(theta) + 0.0065
    val bd_lat: Double = z * Math.sin(theta) + 0.006
    (bd_lng, bd_lat)
  }

  /**
    * GCJ02(火星坐标系) 转换为 WGS84(地球坐标系)
    *
    * @param lng 火星坐标系经度
    * @param lat 火星坐标系纬度
    * @return 地球坐标系经纬度
    */
  def gcj02towgs84(lng: Double, lat: Double): (Double, Double) = {
    var dlat: Double = transformlat(lng - 105.0, lat - 35.0)
    var dlng: Double = transformlng(lng - 105.0, lat - 35.0)
    val radlat: Double = lat / 180.0 * PI
    var magic: Double = Math.sin(radlat)
    magic = 1 - ee * magic * magic
    val sqrtmagic: Double = Math.sqrt(magic)
    dlat = (dlat * 180.0) / ((a * (1 - ee)) / (magic * sqrtmagic) * PI)
    dlng = (dlng * 180.0) / (a / sqrtmagic * Math.cos(radlat) * PI)
    val mglat: Double = lat + dlat
    val mglng: Double = lng + dlng
    (lng * 2 - mglng, lat * 2 - mglat)
  }

  /**
    * WGS84(地球坐标系)转GCj02(火星坐标系)
    *
    * @param lng 地球坐标系经度
    * @param lat 地球坐标系纬度
    * @return 火星坐标系经纬度
    */
  def wgs84togcj02(lng: Double, lat: Double): (Double, Double) = {
    var dlat: Double = transformlat(lng - 105.0, lat - 35.0)
    var dlng: Double = transformlng(lng - 105.0, lat - 35.0)
    val radlat: Double = lat / 180.0 * PI
    var magic: Double = Math.sin(radlat)
    magic = 1 - ee * magic * magic
    val sqrtmagic: Double = Math.sqrt(magic)
    dlat = (dlat * 180.0) / ((a * (1 - ee)) / (magic * sqrtmagic) * PI)
    dlng = (dlng * 180.0) / (a / sqrtmagic * Math.cos(radlat) * PI)
    val mglat: Double = lat + dlat
    val mglng: Double = lng + dlng
    (mglng, mglat)
  }


  /**
    * GCJ02(火星坐标系) 转换为 WGS84(地球坐标系) 纬度转换
    *
    * @param lng
    * @param lat
    * @return
    */
  def transformlat(lng: Double, lat: Double): Double = {
    var ret: Double = -100.0 + 2.0 * lng + 3.0 * lat + 0.2 * lat * lat + 0.1 * lng * lat + 0.2 * Math.sqrt(Math.abs(lng))
    ret += (20.0 * Math.sin(6.0 * lng * PI) + 20.0 * Math.sin(2.0 * lng * PI)) * 2.0 / 3.0
    ret += (20.0 * Math.sin(lat * PI) + 40.0 * Math.sin(lat / 3.0 * PI)) * 2.0 / 3.0
    ret += (160.0 * Math.sin(lat / 12.0 * PI) + 320 * Math.sin(lat * PI / 30.0)) * 2.0 / 3.0
    ret
  }

  /**
    * GCJ02(火星坐标系) 转换为 WGS84(地球坐标系) 纬度转换
    *
    * @param lng
    * @param lat
    * @return
    */
  def transformlng(lng: Double, lat: Double): Double = {
    var ret: Double = 300.0 + lng + 2.0 * lat + 0.1 * lng * lng + 0.1 * lng * lat + 0.1 * Math.sqrt(Math.abs(lng))
    ret += (20.0 * Math.sin(6.0 * lng * PI) + 20.0 * Math.sin(2.0 * lng * PI)) * 2.0 / 3.0
    ret += (20.0 * Math.sin(lng * PI) + 40.0 * Math.sin(lng / 3.0 * PI)) * 2.0 / 3.0
    ret += (150.0 * Math.sin(lng / 12.0 * PI) + 300.0 * Math.sin(lng / 30.0 * PI)) * 2.0 / 3.0
    ret
  }

  /**
    * 判断一个点是否在电子围栏内
    *
    * @param point 判断点
    * @param pts 判断电子围栏
    * @return
    */
  def isInPolygon(point: Point2D.Double, pts: Seq[Point2D.Double]): Boolean = {
    val N: Int = pts.length
    val boundOrVertex = true
    var intersectCount = 0
    //交叉点数量
    val precision = 2e-10; //浮点类型计算时候与0比较时候的容差
    var p1: Point2D.Double = pts.head
    var p2: Point2D.Double = null.asInstanceOf[Point2D.Double] //临近顶点
    val p: Point2D.Double = point //当前点


    val breakable = Breakable()
    breakable.foreach(pts, (po: Any) => {

      if (p == p1) return boundOrVertex

      p2 = pts(pts.indexOf(po) % N)
      if (p.x < Math.min(p1.x, p2.x) || p.x > Math.max(p1.x, p2.x)) {
        p1 = p2
        breakable.continue()
      }

      //射线穿过算法
      if (p.x > Math.min(p1.x, p2.x) && p.x < Math.max(p1.x, p2.x)) if (p.y <= Math.max(p1.y, p2.y)) {
        if (p1.x == p2.x && p.y >= Math.min(p1.y, p2.y)) return boundOrVertex
        if (p1.y == p2.y) if (p1.y == p.y) return boundOrVertex
        else intersectCount += 1
        else {
          val xinters: Double = (p.x - p1.x) * (p2.y - p1.y) / (p2.x - p1.x) + p1.y
          if (Math.abs(p.y - xinters) < precision) return boundOrVertex
          if (p.y < xinters) intersectCount += 1
        }
      }
      else if (p.x == p2.x && p.y <= p2.y) {
        val p3: Point2D.Double = pts((pts.indexOf(po) + 1) % N)
        if (p.x >= Math.min(p1.x, p3.x) && p.x <= Math.max(p1.x, p3.x)) intersectCount += 1
        else intersectCount += 2
      }
      p1 = p2
    })
    if (intersectCount % 2 == 0) { //偶数在多边形外
      false
    }
    else { //奇数在多边形内
      true
    }

  }

}
