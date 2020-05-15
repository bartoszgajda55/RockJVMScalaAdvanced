package lectures.part3concurrency

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Success
import scala.util.Failure

object FuturesPromises extends App {

  def calculateMeaningOfLife: Int = {
    Thread.sleep(2000)
    42
  }

  val aFuture = Future {
    calculateMeaningOfLife
  }

  println(aFuture.value) // Option[Try[Int]]

  aFuture.onComplete {
    case Success(value)     => println(value)
    case Failure(exception) => println(exception)
  }

  Thread.sleep(3000)
}
