import java.sql.Timestamp
import org.apache.spark.sql.SparkSession
import scala.collection.mutable
import scala.util.Random

import akka.actor.{Props, _}
import com.typesafe.config.ConfigFactory
import org.apache.log4j.{Level, Logger}

import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.akka.{ActorReceiver, AkkaUtils}

case class SubscribeReceiver(receiverActor: ActorRef)
case class UnsubscribeReceiver(receiverActor: ActorRef)

class FeederActor extends Actor {

  val rand = new Random()
  val receivers = new mutable.LinkedHashSet[ActorRef]()

  val strings: Array[String] = Array("words ", "may ", "count ")

  def makeMessage(): String = {
    val x = rand.nextInt(3)
    strings(x) + strings(2 - x)
  }
  new Thread() {
    override def run() {
      while (true) {
        Thread.sleep(500)
        receivers.foreach(_ ! makeMessage)
      }
    }
  }.start()

  def receive: Receive = {
    case SubscribeReceiver(receiverActor: ActorRef) =>
      println(s"received subscribe from ${receiverActor.toString}")
      receivers += receiverActor

    case UnsubscribeReceiver(receiverActor: ActorRef) =>
      println(s"received unsubscribe from ${receiverActor.toString}")
      receivers -= receiverActor
  }
  class SampleActorReceiver[T](urlOfPublisher: String) extends ActorReceiver {

    lazy private val remotePublisher = context.actorSelection(urlOfPublisher)

    override def preStart(): Unit = remotePublisher ! SubscribeReceiver(context.self)

    def receive: PartialFunction[Any, Unit] = {
      case msg => store(msg.asInstanceOf[T])
    }

    override def postStop(): Unit = remotePublisher ! UnsubscribeReceiver(context.self)

  }
}
object WordCount {

  def main(args: Array[String]): Unit = {
    //  if (args.length < 2) {
    //    System.err.println("Usage: FeederActor <hostname> <port>\n")
    //    System.exit(1)
    //  }
    //val Seq(host, port) = args.toSeq

    val akkaConf = ConfigFactory.parseString(
      s"""akka.actor.provider = "akka.remote.RemoteActorRefProvider"
         |akka.remote.enabled-transports = ["akka.remote.netty.tcp"]
         |akka.remote.netty.tcp.hostname = "localhost"
         |akka.remote.netty.tcp.port = 8222
         |""".stripMargin)
    val actorSystem = ActorSystem("test", akkaConf)
    val feeder = actorSystem.actorOf(Props[FeederActor], "FeederActor")

    println("Feeder started as:" + feeder)

    actorSystem.awaitTermination()
  }

}
