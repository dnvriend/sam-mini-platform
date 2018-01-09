package com.github.dnvriend

import java.util.UUID

import com.amazonaws.services.kinesis.AmazonKinesisClientBuilder
import com.github.dnvriend.lambda._
import com.github.dnvriend.lambda.annotation.KinesisConf
import com.github.dnvriend.lambda.annotation.policy._
import com.github.dnvriend.platform.model.order.Order
import com.github.dnvriend.sam.resolver.dynamodb.DynamoDBSchemaResolver
import com.github.dnvriend.sam.serialization.DTry
import com.github.dnvriend.sam.serialization.record.SamRecord
import com.github.dnvriend.sam.serialization.serializer.SamSerializer
import play.api.libs.json._

@AmazonDynamoDBFullAccess
@CloudWatchFullAccess
@AmazonKinesisFullAccess
@AWSLambdaVPCAccessExecutionRole
@AWSKeyManagementServicePowerUser
@KinesisConf(stream = "import:order-intake:order-intake-stream", startingPosition = "TRIM_HORIZON")
class OrderBusinessService extends KinesisEventHandler {
    def randomId: String = UUID.randomUUID().toString
    override def handle(events: List[KinesisEvent], ctx: SamContext): Unit = {
        val resolver = new DynamoDBSchemaResolver(ctx, "import:sam-schema-repo:schema_by_fingerprint")
        events.foreach { event =>
            val record: SamRecord = event.dataAs[SamRecord]
            println("Deserialized record: " + record)
            val result: DTry[Order] = SamSerializer.deserialize[Order](record, resolver, None)
            result foreach { order =>
                if(order.orderId.isEmpty) order.copy(orderId = randomId)
                println("Deserialized order: " + order)
                OrderDBInserter.insertOrder(order)
                PublishOrder.publish(
                    OrderReleaseModel(
                        order.orderId,
                        order.clientId,
                        order.name,
                        order.orderLines.toString,
                        order.timestamp
                    ), ctx)
            }
        }
    }
}

/**
  * Publishes a released model to order-release-stream
  */
object PublishOrder {
    val kinesis = AmazonKinesisClientBuilder.defaultClient()

    def publish(client: OrderReleaseModel, ctx: SamContext): Unit = {
        val stage: String = ctx.stage
        val streamName: String = s"order-release-$stage-order-release-stream"
        val recordJson: String = Json.toJson(client).toString
        val recordJsonEOL = recordJson + "\n"
        val recordBytes = java.nio.ByteBuffer.wrap(recordJsonEOL.getBytes("UTF-8"))
        kinesis.putRecord(streamName, recordBytes, "STATIC_PARTITION_KEY")
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