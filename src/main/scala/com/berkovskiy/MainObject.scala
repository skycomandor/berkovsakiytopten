package com.berkovskiy

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.settings.ParserSettings.ErrorLoggingVerbosity.Off
import org.apache.log4j.Logger
import org.apache.log4j.Level

import org.apache.log4j.Level.OFF


/**
  * Created by oberkov on 7/7/17.
  */
object MainObject extends App{


  Logger.getLogger("org").setLevel(OFF)
  Logger.getLogger("akka").setLevel(OFF)


  val system = ActorSystem("system")
  val outputCounterActor = system.actorOf(Props[OutputCounterActor])

  outputCounterActor ! "http://doc.akka.io/docs/akka/current/scala/actors.html"

}
