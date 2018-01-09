package com.github.dnvriend

import java.sql.Connection
import java.util.UUID

import com.github.dnvriend.platform.model.order.Order

import scalaz.Scalaz._

object SqlGenerator {
  private def toDateFormat(time: Long): String = {
    //'2018-01-08 14:29:33'
    new java.text.SimpleDateFormat("YYYY-MM-dd HH:mm:ss").format(new java.util.Date(time))
  }

  def insertOrder(order: Order): List[String] = {
    val orderSQL: String =
      s"""
         |INSERT INTO public.orders(order_id, client_id, name, timestamp) VALUES (
         | '${order.orderId}',
         | '${order.clientId}',
         | '${order.name}',
         | '${toDateFormat(order.timestamp)}'
         |);
       """.stripMargin

    val orderLinesSQL: List[String] = order.orderLines.map { orderLine =>
      s"""
         |INSERT into public.order_lines(order_line_id, order_id, product_id, name, num_items, price) VALUES (
         |'${UUID.randomUUID().toString}',
         |'${order.orderId}',
         |'${orderLine.productId}',
         |'${orderLine.name}',
         |${orderLine.numItems},
         |${orderLine.price}
         |);
       """.stripMargin
    }

    orderSQL +: orderLinesSQL
  }
}

object OrderDBInserter {
  def insertOrders(orders: List[Order], connection: Connection): Int = {
    val sql: String = orders.flatMap(SqlGenerator.insertOrder).intercalate("\n")
    println(sql)
    connection.createStatement().executeUpdate(sql)
  }
}