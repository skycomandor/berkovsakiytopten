package com.berkovskiy

import akka.actor.{Actor, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.ActorMaterializer
import akka.util.ByteString
import com.berkovskiy.Hello.{cleanedString, responseFuture, result, sparkSession}
import org.apache.spark.sql.SparkSession
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.{Await, Future}

/**
  * Created by oberkov on 7/7/17.
  */
class OutputCounterActor extends Actor{


  private val sparkSession = SparkSession.builder
    .master("local")
    .appName("TopTen")
    .getOrCreate()

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  override def receive: Receive = {
    case url: String =>
      val response: Future[HttpResponse] =
        Http().singleRequest(HttpRequest(uri = url))

      import scala.concurrent.duration._
      val result = Await.result(response, 1.seconds)

      if(result.status.isSuccess()) {

      val cleanedString: Future[List[String]] =
        result.entity.dataBytes.runFold(ByteString(""))(_++_).map { bpdy =>
          bpdy.utf8String
            .replaceAll("<script[^>]+>((.|\\n|\\r)+?)</script>", "")
            .replaceAll("<style[^>]+>((.|\\n|\\r)+?)</style>", "")
            .replaceAll("<[^<]+?>", "")
            .trim()
            .split("\\W+").toList
        }
          val cleanResult: List[String] = Await.result(cleanedString, 1.second)
          val rdd = sparkSession.sparkContext.parallelize(cleanResult)

         val map = rdd.map(w => (w, 1))
            .reduceByKey(_ + _)
            .sortBy(_._2, false)
            .take(10).toMap
        println(s"URl is $url and ints result")
        map.foreach(println)

      } else {
        println(s"Cannot get result on url === $url")
      }
  }


}
