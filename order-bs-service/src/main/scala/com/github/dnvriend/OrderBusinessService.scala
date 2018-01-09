package com.github.dnvriend

import java.sql.{Connection, DriverManager}
import java.util.{Properties, UUID}

import com.amazonaws.services.kinesis.AmazonKinesisClientBuilder
import com.github.dnvriend.lambda._
import com.github.dnvriend.lambda.annotation.KinesisConf
import com.github.dnvriend.lambda.annotation.policy._
import com.github.dnvriend.platform.model.order.Order
import com.github.dnvriend.sam.resolver.dynamodb.DynamoDBSchemaResolver
import com.github.dnvriend.sam.serialization.record.SamRecord
import com.github.dnvriend.sam.serialization.serializer.SamSerializer
import play.api.libs.json._

import scalaz._

@AmazonDynamoDBFullAccess
@CloudWatchFullAccess
@AmazonKinesisFullAccess
@AWSLambdaVPCAccessExecutionRole
@AWSKeyManagementServicePowerUser
@KinesisConf(stream = "import:order-intake:order-intake-stream", startingPosition = "TRIM_HORIZON")
class OrderBusinessService extends KinesisEventHandler {
  Class.forName("org.postgresql.Driver").newInstance()
  private val dbendpoint = "rds-test-martijn.c5mvtqg6mxyp.eu-west-1.rds.amazonaws.com"
  private val dbName = "rdstest"
  private val dbUserName = "martijn"
  private val dbPassword = "mypassword"
  private val dbPort = 5432
  private val dbUrl = s"jdbc:postgresql://$dbendpoint:$dbPort/$dbName?reWriteBatchedInserts=true"
  private val properties: Properties = new Properties()
  properties.setProperty("useSSL", "false")
  properties.setProperty("user", dbUserName)
  properties.setProperty("password", dbPassword)
  private val connection: Connection = DriverManager.getConnection(dbUrl, properties)

  def randomId: String = UUID.randomUUID().toString

  val cmkArn: String = "arn:aws:kms:eu-west-1:015242279314:key/04a8c913-9c2b-42e8-a4b5-1bd2beccc3f2"

  override def handle(events: List[KinesisEvent], ctx: SamContext): Unit = {
    val result = Disjunction.fromTryCatchNonFatal {
      val resolver = new DynamoDBSchemaResolver(ctx, "import:sam-schema-repo:schema_by_fingerprint")
      val orders: List[Order] = events.map { event =>
        val record: SamRecord = event.dataAs[SamRecord]
        SamSerializer.deserialize[Order](record, resolver, None).fold(
          _ => GenOrder.iterator.next(), identity
        )
      }
      val recordsInserted: Int = OrderDBInserter.insertOrders(orders, connection)
      println(s"Received: ${orders.size} records, Inserted: $recordsInserted records!!")
      orders.foreach { order =>
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

    println(result.bimap(t => t.getMessage, _ => "ok"))
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