package lectures.part3concurrency

object ThreadCommunication extends App {

  // Producer-consumer problem

  class SimpleContainer {
    private var value: Int = 0

    def isEmpty: Boolean = value == 0
    def set(newValue: Int) = value = newValue
    def get = {
      val result = value
      value = 0
      result
    }
  }

  def naiveProdCons(): Unit = {
    val container = new SimpleContainer
    val consumer = new Thread(() => {
      println("consumer waiting")
      while (container.isEmpty) {
        println("consumer actively waiting")
      }

      println("consumer have consumed " + container.get)
    })

    val producer = new Thread(() => {
      println("producer computing")
      Thread.sleep(500)
      val value = 42
      println("producer have produced value " + value)
      container.set(value)
    })

    consumer.start()
    producer.start()
  }

  // naiveProdCons()

  // wait and notify
  def smartProdCons(): Unit = {
    val container = new SimpleContainer

    val consumer = new Thread(() => {
      println("consumer waiting")
      container.synchronized {
        container.wait()
      }

      // container must have some value
      println("consumer have consumed " + container.get)
    })

    val producer = new Thread(() => {
      println("producer at work")
      Thread.sleep(2000)
      val value = 42

      container.synchronized {
        println("producer producing " + value)
        container.set(value)
        container.notify()
      }
    })

    consumer.start()
    producer.start()
  }

  smartProdCons()

}
