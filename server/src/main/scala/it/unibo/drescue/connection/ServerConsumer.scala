package it.unibo.drescue.connection

import com.rabbitmq.client.DefaultConsumer
import it.unibo.drescue.database.DBConnectionImpl

/**
  * Class that represents a general server side consumer.
  *
  * @param rabbitMQ         connection to rabbitMQ
  * @param serviceOperation delegate with methods to handle requests and do right actions
  *                         (create response message or forward it)
  */
case class ServerConsumer(private val rabbitMQ: RabbitMQ,
                          private val serviceOperation: ServiceOperation)
  extends DefaultConsumer(rabbitMQ.getChannel) {

  val dbConnection: DBConnectionImpl = DBConnectionImpl.getLocalConnection

  import com.rabbitmq.client.{AMQP, Envelope}

  override def handleDelivery(consumerTag: String,
                              envelope: Envelope,
                              properties: AMQP.BasicProperties,
                              body: Array[Byte]): Unit = {
    super.handleDelivery(consumerTag, envelope, properties, body)

    val message = new String(body, "UTF-8")
    println("[ServerConsumer] " + message)

    import it.unibo.drescue.communication.messages.Message
    import it.unibo.drescue.communication.messages.response.ErrorMessageImpl

    var response: Message = null
    try {
      response = serviceOperation.accessDB(dbConnection, message)
    } catch {
      //TODO handle different type of exception and
      //log error message or return an error
      case e: Exception => response = new ErrorMessageImpl("Error connecting database.")
    }

    if (response != null) {
      serviceOperation.handleDBresult(rabbitMQ, properties, response)
    }

  }

}
