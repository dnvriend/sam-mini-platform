package com.github.dnvriend

import com.amazonaws.services.kinesis.AmazonKinesisClientBuilder
import com.github.dnvriend.lambda.annotation.KinesisConf
import com.github.dnvriend.lambda.{KinesisEvent, KinesisEventHandler, SamContext}
import com.github.dnvriend.platform.model.order.Order
import com.github.dnvriend.sam.resolver.dynamodb.DynamoDBSchemaResolver
import com.github.dnvriend.sam.serialization.record.SamRecord
import com.github.dnvriend.sam.serialization.serializer.SamSerializer
import play.api.libs.json.{Format, Json}

import scalaz.Scalaz._

/**
  * Publishes a released model to order-release-gov-stream
  */
object PublishOrder {
  val kinesis = AmazonKinesisClientBuilder.defaultClient()

  def publish(order: OrderReleaseModel, ctx: SamContext): Unit = {
    ctx.logger.log("Publishing order: " + order)
    val stage: String = ctx.stage
    val streamName: String = s"order-release-gov-$stage-order-release-gov-stream"
    val recordJson: String = Json.toJson(order).toString
    val recordJsonEOL = recordJson + "\n"
    val recordBytes = java.nio.ByteBuffer.wrap(recordJsonEOL.getBytes("UTF-8"))
    kinesis.putRecord(streamName, recordBytes, "STATIC_PARTITION_KEY")
  }

  def mapToOrderReleaseModel(order: Order): OrderReleaseModel = {
    OrderReleaseModel(
      order.orderId,
      order.clientId,
      order.name,
      order.orderLines.toString,
      order.timestamp
    )
  }
}

object OrderReleaseModel {
  implicit val format: Format[OrderReleaseModel] = Json.format
}

case class OrderReleaseModel(
                              orderId: String,
                              clientId: String,
                              name: String,
                              orderLines: String,
                              timestamp: Long
                            )

/**
  * OrderBusinessServiceGov receives messages from the stream 'order-master-gov' and logs them to the console.
  */
@KinesisConf(stream = "import:order-master-gov:order-master-gov-stream", startingPosition = "TRIM_HORIZON", batchSize = 25)
class OrderBsServiceGov extends KinesisEventHandler {
  override def handle(events: List[KinesisEvent], ctx: SamContext): Unit = {
    val resolver = new DynamoDBSchemaResolver(ctx, "import:sam-schema-repo:schema_by_fingerprint")
    events.foreach(event => ctx.logger.log("Received Kinesis Event: " + event.toString))
    val result: DTry[List[Order]] = for {
      records <- events.map(_.dataAs[SamRecord]).safe
      _ = ctx.logger.log("Received SamRecords: " + records.toString)
      orders <- records.traverseU(record => SamSerializer.deserialize[Order](record, resolver, None))
      _ = ctx.logger.log("Received Orders: " + orders.toString)
    } yield orders
    result.foreach(xs => xs.foreach(value => ctx.logger.log(value.toString)))
    result.foreach(xs => xs.map(value => PublishOrder.mapToOrderReleaseModel(value)).foreach(orm => PublishOrder.publish(orm, ctx)))
    ctx.logger.log(result.bimap(t => t.toString, _ => "ok").merge)
  }
}