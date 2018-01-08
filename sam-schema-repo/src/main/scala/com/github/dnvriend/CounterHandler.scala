package com.github.dnvriend

import com.github.dnvriend.lambda.JsonReads._
import com.github.dnvriend.lambda._
import com.github.dnvriend.lambda.annotation.HttpHandler
import play.api.libs.json.Json

import scala.util.Try

/**
  * CounterHandler returns the next value for a counter.
  * This handler exists only as a demonstration.
  *
  */
@HttpHandler(path = "/counters/{counter}", method = "get")
class CounterHandler extends JsonApiGatewayHandler[Nothing] {
  override def handle(body: Option[Nothing],
                      pathParams: Map[String, String],
                      requestParams: Map[String, String],
                      request: HttpRequest,
                      ctx: SamContext): HttpResponse = {

    pathParams.get("counter")
      .fold(Response.validationError("No 'counter' found in path parameters"))(counterKey => {
        Try(Repositories.counterRepository(ctx).incrementAndGet(counterKey))
          .fold(Response.serverError, {
          case Some(value) => Response.ok(Json.obj("counter" -> counterKey, "value" -> value))
          case _ => Response.validationError(s"Counter not found: " + counterKey)
        })
      })
  }
}
