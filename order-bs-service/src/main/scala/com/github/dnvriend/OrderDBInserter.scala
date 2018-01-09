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
  private val dbUrl = s"jdbc:postgresql://$dbendpoint:$dbPort/postgres"

  private val properties: Properties = new Properties()
  properties.setProperty("useSSL", "false")
  properties.setProperty("user", dbUserName)
  properties.setProperty("password", dbPassword)

  def insertOrder(order: Order): Unit = {
    val connection = DriverManager.getConnection(dbUrl, properties)

    val orderSQL: String =
    s"""
         |INSERT INTO orders(order_id, client_id, name, timestamp) VALUES (
         | ${order.orderId},
         | ${order.clientId},
         | ${order.name},
         | ${order.timestamp}
         |);
       """.stripMargin

    def orderLinesSQL(order: Order): List[String] = order.orderLines.map{orderLine =>
      s"""
         |INSERT into order_lines(order_line_id, order_id, product_id, name, num_items, price) VALUES (
         |${UUID.randomUUID().toString}
         |${order.orderId}
         |${orderLine.productId}
         |${orderLine.name}
         |${orderLine.numItems}
         |${orderLine.price}
       """.stripMargin
    }

    execute(connection.prepareStatement(orderSQL)){stmt =>
      val insertResult = stmt.executeUpdate()
      println(s"Affected rows after Order insert: '$insertResult'")
    }

    execute(connection.createStatement()){stmt =>
      orderLinesSQL(order).foreach(stmt.addBatch)
      val batchResult = stmt.executeBatch()
      println(s"Affected Rows after Order batch insert: '${batchResult.toList.fold}'")
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
