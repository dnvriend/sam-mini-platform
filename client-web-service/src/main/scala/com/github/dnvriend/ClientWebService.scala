package com.github.dnvriend

import java.util.UUID

import com.amazonaws.services.kinesis.AmazonKinesisClientBuilder
import com.github.dnvriend.lambda._
import com.github.dnvriend.lambda.annotation.{HttpHandler, ScheduleConf}
import com.github.dnvriend.platform.model.client.{Client, ContactInformation, LivingAddress}
import com.github.dnvriend.sam.serialization.serializer.SamSerializer
import org.scalacheck.Gen
import play.api.libs.json._

import scala.compat.Platform

/**
  * Publishes a SamRecord as 'structured data' to client intake
  */
object PublishClient {
    val kinesis = AmazonKinesisClientBuilder.defaultClient()

    def publish(client: Client, ctx: SamContext): Unit = {
        val stage: String = ctx.stage
        val streamName: String = s"client-intake-$stage-client-intake-stream"
        SamSerializer.serialize(client, None).fold(
            t => throw t, record => {
            val recordJson: String = Json.toJson(record).toString
            val recordJsonEOL = recordJson + "\n"
            val recordBytes = java.nio.ByteBuffer.wrap(recordJsonEOL.getBytes("UTF-8"))
            kinesis.putRecord(streamName, recordBytes, "STATIC_PARTITION_KEY")
        })
    }
}

/**
  * A generator for creating client information
  */
object GenClient {
    def timestamp: Long = Platform.currentTime

    val genContactInfo = for {
        email <- Gen.alphaStr
        telephone <- Gen.alphaStr
        mobile <- Gen.alphaStr
    } yield ContactInformation(
        email,
        telephone,
        mobile
    )

    val genLivingAddress = for {
        street <- Gen.alphaStr
        houseNr <- Gen.posNum[Int]
        zipcode <- Gen.alphaStr
    } yield LivingAddress(
        street,
        houseNr,
        zipcode
    )
    val genClient = for {
        clientId <- Gen.uuid.map(id => id.toString)
        name <- Gen.alphaStr
        age <- Gen.posNum[Int]
        livingAddress <- genLivingAddress
        contactInfo <- genContactInfo
    } yield Client(
        clientId,
        name,
        age,
        livingAddress,
        contactInfo,
        timestamp
    )

    val iterator: Iterator[Client] = {
        Stream.continually(genClient.sample).collect { case Some(client) => client }.iterator
    }
}

/**
  * publishes 100 random generated contact details every minute
  */
@ScheduleConf(schedule = "rate(1 minute)")
class CreateClientScheduled extends ScheduledEventHandler {
    override def handle(event: ScheduledEvent, ctx: SamContext): Unit = {
        GenClient.iterator.take(100).foreach(
            PublishClient.publish(_, ctx)
        )
    }
}

/**
  * Json model to receive client details
  */
object ClientDetails {
    implicit val format: Format[ClientDetails] = Json.format
}
case class ClientDetails(
                          name: String,
                          age: Int,
                          email: String
                        )


/**
  * A (potential) client will contact the web service to send personal and contact details. These details will
  * be published to kinesis for further processing
  */
@HttpHandler(path = "/client", method = "put")
class PutClientDetails extends JsonApiGatewayHandler[ClientDetails] {
    override def handle(value: Option[ClientDetails],
                        pathParams: Map[String, String],
                        requestParams: Map[String, String],
                        request: HttpRequest,
                        ctx: SamContext): HttpResponse = {
        value.fold(HttpResponse.validationError.withBody(Json.toJson("Could not deserialize ClientDetails")))(details => {
            val timestamp: Long = Platform.currentTime
            val id: String = UUID.randomUUID().toString
            val client: Client = Client(
                id,
                details.name,
                details.age,
                LivingAddress("nop", 0, "nop"),
                ContactInformation(details.email, "nop", "nop"),
                timestamp
            )
            PublishClient.publish(client, ctx)
            HttpResponse.ok.withBody(Json.toJson(client))
        })
    }
}