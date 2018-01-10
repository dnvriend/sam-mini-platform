package com.github.dnvriend

import java.nio.ByteBuffer

import com.amazonaws.services.kinesis.{AmazonKinesis, AmazonKinesisClientBuilder}
import com.github.dnvriend.lambda.annotation.KinesisConf
import com.github.dnvriend.lambda.{KinesisEvent, KinesisEventHandler, SamContext}
import com.github.dnvriend.platform.model.client.Client
import com.github.dnvriend.sam.resolver.dynamodb.DynamoDBSchemaResolver
import com.github.dnvriend.sam.serialization.record.SamRecord
import com.github.dnvriend.sam.serialization.serializer.SamSerializer
import play.api.libs.json.Json

import scalaz.Scalaz._

object Kinesis {
  val kinesis: AmazonKinesis = AmazonKinesisClientBuilder.defaultClient()
  def publish(client: Client, ctx: SamContext): Unit = {
    val stage: String = ctx.stage
    val streamName: String = s"client-master-$stage-client-master-stream"
    SamSerializer.serialize(client, None).fold(
      t => throw t, record => {
        val recordJson: String = Json.toJson(record).toString
        val recordJsonEOL: String = recordJson + "\n"
        val recordBytes: ByteBuffer = ByteBuffer.wrap(recordJsonEOL.getBytes("UTF-8"))
        kinesis.putRecord(streamName, recordBytes, "STATIC_PARTITION_KEY")
      })
  }
}

@KinesisConf(stream = "import:client-intake:client-intake-stream", startingPosition = "TRIM_HORIZON")
class ClientProcessor extends KinesisEventHandler {
  val cmkArn: String = "arn:aws:kms:eu-west-1:015242279314:key/04a8c913-9c2b-42e8-a4b5-1bd2beccc3f2"
  override def handle(events: List[KinesisEvent], ctx: SamContext): Unit = {
    val resolver = new DynamoDBSchemaResolver(ctx, "import:sam-schema-repo:schema_by_fingerprint")
    val result: DTry[List[Client]] = for {
      records <- events.map(_.dataAs[SamRecord]).safe
      clients <- records.traverseU(record => SamSerializer.deserialize[Client](record, resolver, record.encrypted.option(cmkArn)))
    } yield clients
    result.foreach(xs => xs.foreach(value => Kinesis.publish(value, ctx)))
    result.foreach(xs => xs.foreach(value => ctx.logger.log(value.toString)))
    ctx.logger.log(result.bimap(t => t.toString, _ => "ok").merge)
  }
}