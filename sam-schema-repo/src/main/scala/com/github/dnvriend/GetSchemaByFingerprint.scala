package com.github.dnvriend

import com.github.dnvriend.lambda.JsonReads._
import com.github.dnvriend.lambda._
import com.github.dnvriend.lambda.annotation.HttpHandler
import play.api.libs.json.Json

import scala.util.Try

/**
  * Get schema by fingerprint returns an avro schema for a fingerprint that has been stored
  * by the avro payload. The resolved schema can be used to interpret the avro data.
  */
@HttpHandler(path = "/fingerprint/{fingerprint}", method = "get", authorization=true)
class GetSchemaByFingerprint extends JsonApiGatewayHandler[Nothing] {
  override def handle(body: Option[Nothing],
                      pathParams: Map[String, String],
                      requestParams: Map[String, String],
                      request: HttpRequest,
                      ctx: SamContext): HttpResponse = {

    pathParams.get("fingerprint")
      .fold(Response.validationError("No fingerprint found in path parameters"))(fingerprint => {
        Repositories.schemaByFingerprint(ctx).find[SamSchema](fingerprint)
          .fold(Response.validationError(s"No schema found for fingerprint " + fingerprint))(schema => {
            Try(schema.compressedAvroSchemaBase64)
              .map(compressedAvroSchemaBase64 => AvroUtils.decodeBase64(compressedAvroSchemaBase64))
              .map(compressedAvroSchema => AvroUtils.decompress(compressedAvroSchema))
              .map(avroSchemaAsUtf8Array => new String(avroSchemaAsUtf8Array))
              .fold(Response.serverError, avroSchemaJson => HttpResponse.ok.withBody(Json.parse(avroSchemaJson)))
          })
      })
  }
}