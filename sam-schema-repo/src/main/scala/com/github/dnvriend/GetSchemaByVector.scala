package com.github.dnvriend

import com.github.dnvriend.lambda._
import com.github.dnvriend.lambda.annotation.HttpHandler
import play.api.libs.json.{Json, _}

import scalaz.Scalaz._

/**
  * Returns a schema by vector which is defined as 'namespace-name:schema-name:version'
  */
@HttpHandler(path = "/namespaces/{namespaceName}/schemas/{schemaName}/versions/{version}", method = "get", authorization=true)
class GetSchemaByVector extends JsonApiGatewayHandler[JsValue] {

  override def handle(schema: Option[JsValue],
                      pathParams: Map[String, String],
                      requestParams: Map[String, String],
                      request: HttpRequest,
                      ctx: SamContext): HttpResponse = {

    (pathParams.get("namespaceName") |@| pathParams.get("schemaName") |@| pathParams.get("version")) ((_, _, _))
      .fold(Response.validationError("namespaceName and/or schemaName and/or version path parameter not found"))({
        case (namespaceName, schemaName, version) =>
          val key: String = namespaceName + ":" + schemaName
          Repositories.schemaByVector(ctx).find[SamSchema](key, version)
              .map(_.compressedAvroSchemaBase64)
              .map(compressedAvroSchemaBase64 => AvroUtils.decodeBase64(compressedAvroSchemaBase64))
              .map(compressedAvroSchema => AvroUtils.decompress(compressedAvroSchema))
              .map(avroSchemaAsUtf8Array => new String(avroSchemaAsUtf8Array))
            .bimap(error => {
              ctx.logger.log(s"Error from dynamodb: " + error)
              Response.validationError(s"No schema found for key: " + key + ":" + version)
            }, avroSchemaJson => HttpResponse.ok.withBody(Json.parse(avroSchemaJson)))
            .merge
      })
  }
}