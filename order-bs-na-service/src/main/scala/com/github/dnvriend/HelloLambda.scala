package com.github.dnvriend

import java.nio.ByteBuffer

import com.amazonaws.services.kinesis.AmazonKinesisClientBuilder
import com.github.dnvriend.lambda.annotation.{HttpHandler, KinesisConf}
import com.github.dnvriend.lambda._
import play.api.libs.json._

object Person {
    implicit val format: Format[Person] = Json.format
}
case class Person(name: String)

@HttpHandler(path = "/person", method = "post")
class PostPerson extends ApiGatewayHandler {
    override def handle(request: HttpRequest, ctx: SamContext): HttpResponse = {
        val kinesis = AmazonKinesisClientBuilder.defaultClient()
        val person = request.bodyOpt[Person]
        person.fold(HttpResponse.validationError.withBody(Json.toJson("error parsing")))(person => {
            val json: String = Json.toJson(person).toString
            val bytes = ByteBuffer.wrap(json.getBytes("UTF-8"))
            kinesis.putRecord(ctx.kinesisStreamName("person-received"), bytes, "STATIC_PARTITION_KEY")
            HttpResponse.ok.withBody(Json.toJson(person))
        })
    }
}

@KinesisConf(stream = "person-received")
class PersonCreatedKinesisHandler extends KinesisEventHandler {
    override def handle(events: List[KinesisEvent], ctx: SamContext): Unit = {
        events.foreach { event =>
            println(event.dataAs[Person])
        }
    }
}