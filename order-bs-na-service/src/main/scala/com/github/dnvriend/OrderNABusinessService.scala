package com.github.dnvriend

import java.nio.ByteBuffer

import com.amazonaws.services.kinesis.AmazonKinesisClientBuilder
import com.github.dnvriend.lambda.annotation.{HttpHandler, KinesisConf}
import com.github.dnvriend.lambda._
import com.github.dnvriend.platform.model.order.Order
import com.github.dnvriend.sam.resolver.dynamodb.DynamoDBSchemaResolver
import com.github.dnvriend.sam.serialization.record.SamRecord
import com.github.dnvriend.sam.serialization.serializer.SamSerializer
import play.api.libs.json._

@KinesisConf(stream = "import:order-intake:order-intake-stream")
class OrderNABusinessService extends KinesisEventHandler {
    override def handle(events: List[KinesisEvent], ctx: SamContext): Unit = {
        val resolver = new DynamoDBSchemaResolver(ctx, "import:sam-schema-repo:schema_by_fingerprint")
        events.foreach { event =>
            val record = event.dataAs[SamRecord]
            val result = SamSerializer.deserialize[Order](record, resolver, None)
            println(result)
        }
    }
}