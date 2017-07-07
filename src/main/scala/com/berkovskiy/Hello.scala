package com.berkovskiy

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.ActorMaterializer
import org.apache.spark.sql.SparkSession
import akka.http.scaladsl.model._
import akka.util.ByteString

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by oberkov on 7/7/17.
  */
object Hello extends App {

 val sparkSession = SparkSession.builder
   .master("local")
   .appName("TopTen")
   .getOrCreate()

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  val responseFuture: Future[HttpResponse] =
    Http().singleRequest(HttpRequest(uri = "http://akka.io"))

  import scala.concurrent.duration._
   val result = Await.result(responseFuture, 1.seconds)
   val cleanedString =
     result.entity.dataBytes.runFold(ByteString(""))(_++_).map{bpdy =>
      bpdy.utf8String
      .replaceAll("<script[^>]+>((.|\\n|\\r)+?)</script>", "")
      .replaceAll("<style[^>]+>((.|\\n|\\r)+?)</style>", "")
      .replaceAll("<[^<]+?>", "")
      .trim()
      .split("\\W+").toList
  }
  val cleanResult: List[String] = Await.result(cleanedString, 1.second)
  val rdd = sparkSession.sparkContext.parallelize(cleanResult)
  rdd.map(w => (w, 1))
    .reduceByKey(_+_)
    .sortBy(_._2, false)
    .take(10)
    .foreach(println)




}
