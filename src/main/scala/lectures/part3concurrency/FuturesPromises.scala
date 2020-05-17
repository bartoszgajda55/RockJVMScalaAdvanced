package lectures.part3concurrency

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Success
import scala.util.Failure
import scala.util.Random
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.Promise

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

  case class Profile(id: String, name: String) {
    def poke(anotherProfile: Profile) =
      println(s"${this.name} is poking ${anotherProfile.name}")
  }

  object SocialNetwork {
    val names = Map(
      "fb.id.1-zuck" -> "Mark",
      "fb.id.2-bill" -> "Bill",
      "fb.id.0-dummy" -> "Dummy"
    )

    val friends = Map(
      "fb.id.1-zuck" -> "fb.id.2-bill"
    )

    val random = new Random()

    // API
    def fetchProfile(id: String): Future[Profile] = Future {
      // Fetching from network like
      Thread.sleep(random.nextInt(300))
      Profile(id, names(id))
    }

    def fetchBestFriend(profile: Profile): Future[Profile] = Future {
      Thread.sleep(random.nextInt(400))
      val bfId = friends(profile.id)
      Profile(bfId, names(bfId))
    }
  }

  val mark = SocialNetwork.fetchProfile("fb.id.1-zuck")
  mark.onComplete {
    case Success(markProfile) => {
      val bill = SocialNetwork.fetchBestFriend(markProfile)
      bill.onComplete {
        case Success(billProfile) => markProfile.poke(billProfile)
        case Failure(exception)   => exception.printStackTrace()
      }
    }
    case Failure(exception) => exception.printStackTrace()
  }
  Thread.sleep(1000)

  // functional compositiion of futures
  // map, flatMap, filter
  val nameOnTheWall = mark.map(profile => profile.name)

  val marksBestFriend =
    mark.flatMap(profile => SocialNetwork.fetchBestFriend(profile))

  val zucksBestFriendRestricted =
    marksBestFriend.filter(profile => profile.name.startsWith("Z"))

  // for-comprehensions
  for {
    mark <- SocialNetwork.fetchProfile("fb.id.1-zuck")
    bill <- SocialNetwork.fetchBestFriend(mark)
  } mark.poke(bill)

  // fallbacks
  val aProfileNoMatterWhat = SocialNetwork.fetchProfile("unknown id").recover {
    case e: Throwable => Profile("fb.id.0-dummy", "Forever Alone")
  }

  val aFetchedProfileNoMatterWaht =
    SocialNetwork.fetchProfile("unknown id").recoverWith {
      case e: Throwable => SocialNetwork.fetchProfile("fb.id.0-dummy")
    }

  val fallbackResult = SocialNetwork
    .fetchProfile("unknown id")
    .fallbackTo(SocialNetwork.fetchProfile("fb.id.0-dummy"))

  case class User(name: String)
  case class Transaction(
      sender: String,
      receiver: String,
      amount: Double,
      status: String
  )

  object BankingApp {
    val name = "The Bank App"

    def fetchUser(name: String): Future[User] = Future {
      // some long computation
      Thread.sleep(500)
      User(name)
    }

    def createTransaction(
        user: User,
        merchantName: String,
        amount: Double
    ): Future[Transaction] = Future {
      // another long computation
      Thread.sleep(1000)
      Transaction(user.name, merchantName, amount, "success")
    }

    def purchase(
        username: String,
        item: String,
        merchantName: String,
        cost: Double
    ): String = {
      val transactionStatysFuture = for {
        user <- fetchUser(username)
        transaction <- createTransaction(user, merchantName, cost)
      } yield transaction.status

      Await.result(transactionStatysFuture, 2.seconds) // implicit conversions
    }
  }

  println(BankingApp.purchase("John", "TV", "Amazon", 1000))

  // Promises
  val promise = Promise[Int]()
  val future = promise.future

  future.onComplete {
    case Success(value)     => println("consumer have received a value " + value)
    case Failure(exception) => println(exception.printStackTrace())
  }

  val producer = new Thread(() => {
    println("producer crunching numbers...")
    Thread.sleep(500)
    promise.success(42)
    println("producer done")
  })

  producer.start()
  Thread.sleep(1000)
}
