package com.github.dnvriend

import java.nio.ByteBuffer

import com.amazonaws.services.kinesis.{AmazonKinesis, AmazonKinesisClientBuilder}
import com.github.dnvriend.lambda.SamContext
import com.github.dnvriend.platform.model.client.Client
import com.github.dnvriend.sam.serialization.serializer.SamSerializer
import play.api.libs.json.Json

/**
  * Publishes a SamRecord as 'structured data' to client intake
  */
object PublishClient {
  val kinesis: AmazonKinesis = AmazonKinesisClientBuilder.defaultClient()
  val cmkArn: String = "arn:aws:kms:eu-west-1:015242279314:key/04a8c913-9c2b-42e8-a4b5-1bd2beccc3f2"
  def publish(client: Client, ctx: SamContext): Unit = {
    val stage: String = ctx.stage
    val streamName: String = s"client-intake-$stage-client-intake-stream"
    SamSerializer.serialize(client, None).fold(
      t => throw t, record => {
        val recordJson: String = Json.toJson(record).toString
        val recordJsonEOL: String = recordJson + "\n"
        val recordBytes: ByteBuffer = ByteBuffer.wrap(recordJsonEOL.getBytes("UTF-8"))
        kinesis.putRecord(streamName, recordBytes, "STATIC_PARTITION_KEY")
      })
  }
}