package com.github.dnvriend

import com.github.dnvriend.lambda._
import com.github.dnvriend.lambda.annotation.HttpHandler
import play.api.libs.json.{Json, _}

import scalaz.Scalaz._

/**
  * Returns a list of available schemas
  */
@HttpHandler(path = "/namespaces/{namespaceName}/schemas/{schemaName}/versions", method = "get", authorization=true)
class GetSchemasByVector extends JsonApiGatewayHandler[JsValue] {

  override def handle(schema: Option[JsValue],
                      pathParams: Map[String, String],
                      requestParams: Map[String, String],
                      request: HttpRequest,
                      ctx: SamContext): HttpResponse = {

    (pathParams.get("namespaceName") |@| pathParams.get("schemaName")) ((_, _))
      .fold(Response.validationError("namespaceName and/or schemaName path parameter not found"))({
        case (namespaceName, schemaName) =>
          val key: String = namespaceName + ":" + schemaName
          Repositories.schemaByVector(ctx).find[SamSchema](key)
                .map(xs => xs.map({ case (key, version, schema) => key + ":" + version }))
            .bimap(Response.serverError, vectors => HttpResponse.ok.withBody(Json.toJson(vectors)))
            .merge
      })
  }
}