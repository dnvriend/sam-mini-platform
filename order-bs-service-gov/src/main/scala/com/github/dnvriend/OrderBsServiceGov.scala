package com.github.dnvriend

import com.github.dnvriend.lambda.annotation.KinesisConf
import com.github.dnvriend.lambda.{KinesisEvent, KinesisEventHandler, SamContext}
import com.github.dnvriend.platform.model.order.Order
import com.github.dnvriend.sam.resolver.dynamodb.DynamoDBSchemaResolver
import com.github.dnvriend.sam.serialization.record.SamRecord
import com.github.dnvriend.sam.serialization.serializer.SamSerializer

import scalaz.Scalaz._

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
    ctx.logger.log(result.bimap(t => t.toString, _ => "ok").merge)
  }
}