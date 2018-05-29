/*
 * Copyright (C) 2018 Lightbend Inc. <https://www.lightbend.com>
 */

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{ Flow, Tcp }
import io.netty.buffer.ByteBuf
import io.netty.util.CharsetUtil

object Test extends App {

  implicit val actorSystem = ActorSystem("test")

  implicit val materializer = ActorMaterializer()

  val echo = Flow[ByteBuf].map { bb ⇒
    println(bb.isDirect)
    println(bb.toString(CharsetUtil.UTF_8))
    bb
  }

  Tcp().bind("localhost", 8080).runForeach { conn ⇒
    conn.handleWith(echo)
  }

}
