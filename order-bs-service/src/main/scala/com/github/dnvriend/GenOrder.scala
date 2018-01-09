package com.github.dnvriend

import com.github.dnvriend.platform.model.order.{Order, OrderLine}
import org.scalacheck.Gen

import scala.compat.Platform

object GenOrder {

  def timestamp: Long = Platform.currentTime

  val genOrderLine = for {
    productId <- Gen.uuid.map(_.toString)
    name <- Gen.alphaStr
    numItems <- Gen.posNum[Int]
    price <- Gen.posNum[Int]
  } yield OrderLine(
    productId,
    name,
    numItems,
    price
  )

  val genOrder = for {
    orderId <- Gen.uuid.map(_.toString)
    clientId <- Gen.uuid.map(_.toString)
    name <- Gen.alphaStr
    orderLine <- Gen.listOfN(10, genOrderLine)
  } yield Order(
    orderId,
    clientId,
    name,
    orderLine,
    timestamp
  )

  val iterator: Iterator[Order] = {
    Stream.continually(genOrder.sample).collect { case Some(client) => client }.iterator
  }
}