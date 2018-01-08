package com.github.dnvriend

import com.github.dnvriend.lambda._
import com.github.dnvriend.lambda.annotation.HttpHandler
import org.apache.avro.Schema
import play.api.libs.json.{Json, _}

import scala.util.Try
import scalaz.Scalaz._

/**
  * Persist an avro schema. The implementation does minimal validation on the received schema
  * and must be extended for a specific use case.
  */
@HttpHandler(path = "/namespaces/{namespaceName}/schemas/{schemaName}", method = "put", authorization=true)
class PutSchemaByVector extends JsonApiGatewayHandler[JsValue] {
  def validateSchema(schema: String): Try[Schema] = Try {
    new Schema.Parser()
      .setValidateDefaults(true)
      .setValidate(true)
      .parse(schema)
  }

  override def handle(schema: Option[JsValue],
                      pathParams: Map[String, String],
                      requestParams: Map[String, String],
                      request: HttpRequest,
                      ctx: SamContext): HttpResponse = {

    (pathParams.get("namespaceName") |@| pathParams.get("schemaName") |@| schema) ((_, _, _))
      .fold(Response.validationError("namespaceName and/or schemaName path not found and/or could not deserialize schema JSON"))({
        case (namespaceName, schemaName, schemaJson) =>
          validateSchema(schemaJson.toString).fold(Response.serverError, schema => {
            val counterKey: String = namespaceName + ":" + schemaName
            Repositories.counterRepository(ctx).incrementAndGet(counterKey)
              .fold(Response.validationError(s"No counter found for " + counterKey))(version => {
                val key: String = namespaceName + ":" + schemaName
                val vector: String = key + ":" + version
                val schemaFingerprint: Array[Byte] = AvroUtils.fingerPrint(schema)
                val fingerprintBase64: String = AvroUtils.encodeBase64(schemaFingerprint)
                val fingerprintHexString: String = AvroUtils.encodeHex(schemaFingerprint)
                val schemaEncodedAsUtf8: Array[Byte] = AvroUtils.encodeUtf8(schema.toString(false))
                val compressedSchema: Array[Byte] = AvroUtils.compress(schemaEncodedAsUtf8)
                val compressedAvroSchemaBase64: String = AvroUtils.encodeBase64(compressedSchema)
                val compressedAvroSchemaHexString: String = AvroUtils.encodeHex(compressedSchema)
                val samSchema = SamSchema(vector, namespaceName, schemaName, version, fingerprintBase64, fingerprintHexString, compressedAvroSchemaBase64, compressedAvroSchemaHexString)

                Repositories.schemaByFingerprint(ctx).put(fingerprintHexString, samSchema)
                Repositories.schemaByVector(ctx).put(key, version.toString, samSchema)
                  .bimap(Response.serverError, _ => HttpResponse.ok.withBody(Json.toJson(samSchema)))
                  .merge
              })
          })
      })
  }
}