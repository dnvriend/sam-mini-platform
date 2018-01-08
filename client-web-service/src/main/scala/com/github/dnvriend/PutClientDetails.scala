package com.github.dnvriend

import java.util.UUID

import com.github.dnvriend.lambda._
import com.github.dnvriend.lambda.annotation.HttpHandler
import com.github.dnvriend.platform.model.client.{Client, ContactInformation, LivingAddress}
import play.api.libs.json._

import scala.compat.Platform
import scalaz.Scalaz._

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
    def mapClient(id: String, timestamp: Long, details: ClientDetails): DTry[Client] = {
        Client(
            id,
            details.name,
            details.age,
            LivingAddress("nop", 0, "nop"),
            ContactInformation(details.email, "nop", "nop"),
            timestamp
        )
    }.safe

    def handleDetails(details: ClientDetails, ctx: SamContext): DTry[Client] = {
        val timestamp: Long = Platform.currentTime
        val id: String = UUID.randomUUID().toString
        mapClient(id, timestamp, details)
    }

    def publish(client: Client, ctx: SamContext): DTry[Unit] = {
        PublishClient.publish(client, ctx)
    }.safe

    override def handle(value: Option[ClientDetails],
                        pathParams: Map[String, String],
                        requestParams: Map[String, String],
                        request: HttpRequest,
                        ctx: SamContext): HttpResponse = {
        val result: DTry[Client] = for {
            details <- value.toRightDisjunction(new RuntimeException("Could not deserialize ClientDetails"))
            client <- handleDetails(details, ctx)
            _ <- publish(client, ctx)
        } yield client
        result.bimap(t => HttpResponse.serverError.withBody(Json.obj("error" -> t.getMessage)),
            client => HttpResponse.ok.withBody(Json.toJson(client))).merge
    }
}