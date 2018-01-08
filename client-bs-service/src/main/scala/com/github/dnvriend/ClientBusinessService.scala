package com.github.dnvriend

import com.amazonaws.services.kinesis.AmazonKinesisClientBuilder
import com.github.dnvriend.lambda.{KinesisEvent, KinesisEventHandler, SamContext}
import com.github.dnvriend.lambda.annotation.KinesisConf
import com.github.dnvriend.platform.model.client.Client
import com.github.dnvriend.repo.JsonRepository
import com.github.dnvriend.repo.dynamodb.DynamoDBJsonRepository
import com.github.dnvriend.sam.resolver.dynamodb.DynamoDBSchemaResolver
import com.github.dnvriend.sam.serialization.record.SamRecord
import com.github.dnvriend.sam.serialization.serializer.SamSerializer
import play.api.libs.json.{Format, Json}

import scalaz._
import scalaz.Scalaz._

/**
  * Publishes a SamRecord as 'structured data' to client intake
  */
object PublishClient {
  val kinesis = AmazonKinesisClientBuilder.defaultClient()

  def publish(client: ClientReleaseModel, ctx: SamContext): Unit = {
    val stage: String = ctx.stage
    val streamName: String = s"client-release-$stage-client-release-stream"
    val recordJson: String = Json.toJson(client).toString
    val recordJsonEOL = recordJson + "\n"
    val recordBytes = java.nio.ByteBuffer.wrap(recordJsonEOL.getBytes("UTF-8"))
    kinesis.putRecord(streamName, recordBytes, "STATIC_PARTITION_KEY")
  }
}

object ClientReleaseModel {
  implicit val format: Format[ClientReleaseModel] = Json.format
}
case class ClientReleaseModel(
                               clientId: String,
                               name: String,
                               age: Int,
                               email: String,
                               mobile: String,
                               telephone: String,
                               street: String,
                               houseNr: Int,
                               zipcode: String
                             )

object ClientRepository {
  def clientTable(ctx: SamContext): JsonRepository = {
    DynamoDBJsonRepository("client_table", ctx)
  }
}

@KinesisConf(stream = "import:client-intake:client-intake-stream")
class ClientBusinessService extends KinesisEventHandler {
  override def handle(events: List[KinesisEvent], ctx: SamContext): Unit = {
    val resolver = new DynamoDBSchemaResolver(ctx, "import:sam-schema-repo:schema_by_fingerprint")
    events.foreach { event =>
      val record = event.dataAs[SamRecord]
      val result = SamSerializer.deserialize[Client](record, resolver, None)
      result foreach { client =>
        ClientRepository.clientTable(ctx).put(client.clientId, client)
        val releaseModel = ClientReleaseModel(
          client.clientId,
          client.name,
          client.age,
          client.contactInformation.email,
          client.contactInformation.mobile,
          client.contactInformation.telephone,
          client.livingAddress.street,
          client.livingAddress.houseNr,
          client.livingAddress.zipcode,
        )
        PublishClient.publish(releaseModel, ctx)
      }
      if(result.isLeft) {
        println("Error deserializing client: " + result.swap.foldMap(_.toString))
      }
    }
  }
}