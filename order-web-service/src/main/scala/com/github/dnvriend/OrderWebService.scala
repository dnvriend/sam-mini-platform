package com.github.dnvriend

import java.util.UUID

import com.amazonaws.services.kinesis.AmazonKinesisClientBuilder
import com.github.dnvriend.lambda._
import com.github.dnvriend.lambda.annotation.{HttpHandler, ScheduleConf}
import com.github.dnvriend.platform.model.client.Client
import com.github.dnvriend.platform.model.order.{Order, OrderLine}
import com.github.dnvriend.repo.JsonRepository
import com.github.dnvriend.repo.dynamodb.DynamoDBJsonRepository
import com.github.dnvriend.sam.serialization.serializer.SamSerializer
import org.scalacheck.Gen
import play.api.libs.json._

import scala.compat.Platform

/**
  * Publishes a SamRecord as 'structured data' to order intake
  */
object PublishOrder {
    val kinesis = AmazonKinesisClientBuilder.defaultClient()
    val cmkArn: String = "arn:aws:kms:eu-west-1:015242279314:key/04a8c913-9c2b-42e8-a4b5-1bd2beccc3f2"
    def publish(order: Order, ctx: SamContext): Unit = {
        val stage: String = ctx.stage
        val streamName: String = s"order-intake-$stage-order-intake-stream"
        SamSerializer.serialize(order, None).fold(
            t => throw t, record => {
                val recordJson: String = Json.toJson(record).toString
                val recordJsonEOL = recordJson + "\n"
                val recordBytes = java.nio.ByteBuffer.wrap(recordJsonEOL.getBytes("UTF-8"))
                kinesis.putRecord(streamName, recordBytes, "STATIC_PARTITION_KEY")
            })
    }
}

object ClientRepository {
    def clientTable(ctx: SamContext): JsonRepository = {
        DynamoDBJsonRepository("import:client-bs-service:client_table", ctx)
    }
}

object Main extends App {
    println(GenOrder.iterator.next())
}

object GenOrder {

    def timestamp: Long = Platform.currentTime

    val firstNames: List[String] = scala.io.Source.fromInputStream(getClass.getClassLoader.getResourceAsStream("firstnames.csv")).getLines().toList
    val lastNames: List[String] = scala.io.Source.fromInputStream(getClass.getClassLoader.getResourceAsStream("lastnames.csv")).getLines().toList

    val genName = for {
        fn <- Gen.oneOf(firstNames)
        ln <- Gen.oneOf(lastNames)
    } yield s"$fn $ln"

    val genOrderLine = for {
        productId <- Gen.uuid.map(_.toString)
        name <- genName
        numItems <- Gen.posNum[Int]
        price <- Gen.posNum[Int]
    } yield OrderLine(
        productId,
        name,
        numItems,
        price
    )

    val genOrder = for {
        orderId <- Gen.uuid.map(_.toString)
        clientId <- Gen.uuid.map(_.toString)
        name <- genName
        orderLine <- Gen.listOfN(10, genOrderLine)
    } yield Order(
        orderId,
        clientId,
        name,
        orderLine,
        timestamp
    )

    val iterator: Iterator[Order] = {
        Stream.continually(genOrder.sample).collect { case Some(client) => client }.iterator
    }
}

/**
  * publishes 100 random generated orders every minute
  */
@ScheduleConf(schedule = "rate(1 minute)")
class CreateOrderScheduled extends ScheduledEventHandler {
    override def handle(event: ScheduledEvent, ctx: SamContext): Unit = {
        GenOrder.iterator.take(500).foreach(
            PublishOrder.publish(_, ctx)
        )
    }
}


/**
  * Json model to receive order details
  */
object OrderDetails {
    implicit val format: Format[OrderDetails] = Json.format
}

/**
  * A client will contact the web service to place an order. The order details will
  * be published to kinesis for further processing
  */
case class OrderDetails(
                       order_name: String,
                       num_items: Int,
                       client_id: String
                       )

@HttpHandler(path = "/order", method = "put")
class PutOrderDetails extends JsonApiGatewayHandler[OrderDetails] {
    override def handle(value: Option[OrderDetails],
                        pathParams: Map[String, String],
                        requestParams: Map[String, String],
                        request: HttpRequest,
                        ctx: SamContext): HttpResponse = {
        value.fold(HttpResponse.validationError.withBody(Json.toJson("Could not deserialize OrderDetails")))(details => {
            val timestamp: Long = Platform.currentTime
            val id: String = UUID.randomUUID().toString
            val order: Order = Order(
                id,
                details.client_id,
                details.order_name,
                List(OrderLine(id, details.order_name, details.num_items, 25)),
                timestamp
            )
            PublishOrder.publish(order, ctx)
            HttpResponse.ok.withBody(Json.toJson(order))
        })
    }
}