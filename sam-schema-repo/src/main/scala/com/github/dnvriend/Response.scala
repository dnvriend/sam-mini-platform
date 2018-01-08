package com.github.dnvriend

import com.github.dnvriend.lambda._
import play.api.libs.json.{Json, _}

object Response {
  def serverError(t: Throwable): HttpResponse = HttpResponse.serverError.withBody(Json.obj("error" -> t.getMessage))

  def serverError(message: String): HttpResponse = HttpResponse.serverError.withBody(Json.obj("error" -> message))

  def validationError(message: String): HttpResponse = HttpResponse.validationError.withBody(Json.obj("error" -> message))

  def ok(body: String): HttpResponse = HttpResponse.ok.withBody(Json.toJson(body))

  def ok(body: JsValue): HttpResponse = HttpResponse.ok.withBody(body)
}
