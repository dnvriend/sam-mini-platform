package com.github.dnvriend

import java.nio.ByteBuffer

import com.amazonaws.services.kinesis.{AmazonKinesis, AmazonKinesisClientBuilder}
import com.github.dnvriend.lambda.annotation.KinesisConf
import com.github.dnvriend.lambda.{KinesisEvent, KinesisEventHandler, SamContext}
import com.github.dnvriend.platform.model.order.Order
import com.github.dnvriend.sam.resolver.dynamodb.DynamoDBSchemaResolver
import com.github.dnvriend.sam.serialization.record.SamRecord
import com.github.dnvriend.sam.serialization.serializer.SamSerializer
import play.api.libs.json.Json

import scalaz.Scalaz._

object Kinesis {
  val kinesis: AmazonKinesis = AmazonKinesisClientBuilder.defaultClient()

  def publish(order: Order, ctx: SamContext): Unit = {
    val stage: String = ctx.stage
    val streamName: String = s"order-master-gov-$stage-order-master-gov-stream"
    ctx.logger.log(s"Publishing $order to $streamName")
    SamSerializer.serialize(order, None).fold(
      t => throw t, record => {
        val recordJson: String = Json.toJson(record).toString
        val recordJsonEOL: String = recordJson + "\n"
        val recordBytes: ByteBuffer = ByteBuffer.wrap(recordJsonEOL.getBytes("UTF-8"))
        kinesis.putRecord(streamName, recordBytes, "STATIC_PARTITION_KEY")
      })
  }
}

/**
  * OrderProcessorGov reads orders from the intake list, and decryps the data using the CMK.
  * The data is governed, meaning this service has no access to the CMK, thus a default instance will be created,
  * effectively the service works, but there is no access to the data. The default instance will be published to
  * the stream 'order-master-gov-stream' for downstream components.
  */
@KinesisConf(stream = "import:order-intake:order-intake-stream", startingPosition = "TRIM_HORIZON", batchSize = 2)
class OrderProcessorGov extends KinesisEventHandler {
  val cmkArn: String = "arn:aws:kms:eu-west-1:015242279314:key/04a8c913-9c2b-42e8-a4b5-1bd2beccc3f2"

  override def handle(events: List[KinesisEvent], ctx: SamContext): Unit = {
    val resolver = new DynamoDBSchemaResolver(ctx, "import:sam-schema-repo:schema_by_fingerprint")
    events.foreach(event => ctx.logger.log("Received Kinesis Event: " + event.toString))
    val result: DTry[List[Order]] = for {
      records <- events.map(_.dataAs[SamRecord]).safe
      _ = ctx.logger.log("Received SamRecords: " + records.toString)
      encrypted <- records.filter(_.encrypted).safe
      _ = ctx.logger.log("Received Encrypted SamRecords: " + encrypted.toString)
      orders <- encrypted.traverseU(record => SamSerializer.deserialize[Order](record, resolver, record.encrypted.option(cmkArn)))
      _ = ctx.logger.log("Received Encrypted Orders: " + orders.toString)
    } yield orders
    result.foreach(xs => {
      if (xs.isEmpty) ctx.logger.log("No orders to publish.") else ctx.logger.log("There are orders to publish!")
    })

    result.foreach( xs => {
      xs.foreach(value => Kinesis.publish(value, ctx)).safe.bimap(
        t => "error while publishing" + t.getMessage, _ => s"Successfully published $xs"
      )
    })

    result.foreach(xs => xs.foreach(value => ctx.logger.log(value.toString)))
    ctx.logger.log(result.bimap(t => t.toString, orders => "ok: " + orders).merge)
  }
}