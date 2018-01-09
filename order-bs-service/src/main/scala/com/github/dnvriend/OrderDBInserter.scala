package com.github.dnvriend

import java.sql.DriverManager
import java.util.{Properties, UUID}
import scala.collection.JavaConverters._
import scalaz._
import scalaz.Scalaz._

import com.github.dnvriend.platform.model.order.{Order, OrderLine}

import scala.util.{Failure, Success, Try}

object OrderDBInserter {

  Class.forName("org.postgresql.Driver").newInstance()

  private val dbendpoint = "rds-test-martijn.c5mvtqg6mxyp.eu-west-1.rds.amazonaws.com"
  private val dbName = "rdstest"
  private val dbUserName = "martijn"
  private val dbPassword = "mypassword"
  private val dbPort = 5432
  private val dbUrl = s"jdbc:postgresql://$dbendpoint:$dbPort/$dbName"

  private val properties: Properties = new Properties()
  properties.setProperty("useSSL", "false")
  properties.setProperty("user", dbUserName)
  properties.setProperty("password", dbPassword)

  def toDateFormat(time: Long): String = {
    //'2018-01-08 14:29:33'
    new java.text.SimpleDateFormat("YYYY-MM-dd HH:mm:ss").format(new java.util.Date(time))
  }

  def insertOrder(order: Order): Unit = {
    val connection = DriverManager.getConnection(dbUrl, properties)

    val orderSQL: String =
    s"""
         |INSERT INTO public.orders(order_id, client_id, name, timestamp) VALUES (
         | '${order.orderId}',
         | '${order.clientId}',
         | '${order.name}',
         | '${toDateFormat(order.timestamp)}'
         |);
       """.stripMargin

    def orderLinesSQL(order: Order): List[String] = order.orderLines.map{orderLine =>
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

    println(s"""
       |All sql statements:
       |$orderSQL
       |${orderLinesSQL(order)}
     """.stripMargin)

    execute(connection.prepareStatement(orderSQL)){stmt =>
      val insertResult = stmt.executeUpdate()
      println(s"Affected rows after Order insert: '$insertResult'")
    }

    execute(connection.createStatement()){stmt =>
      orderLinesSQL(order).foreach(stmt.addBatch)
      val batchResult = stmt.executeBatch()
      println(s"Affected Rows after Order batch insert: '${batchResult.toList.foldMap(identity)}'")
    }
    connection.close()
  }

  private def execute[A <: AutoCloseable, B](resource: A)(block: A => B): B = {
    Try(block(resource)) match {
      case Success(result) =>
        resource.close()
        result
      case Failure(e) =>
        resource.close()
        throw e
    }
  }
}
