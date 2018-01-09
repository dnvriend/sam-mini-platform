package com.github.dnvriend

import java.sql.DriverManager
import java.util.Properties

import com.github.dnvriend.platform.model.client.Client

object ClientDBInserter {

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

  def insertClient(client: Client): Unit = {
    val connection = DriverManager.getConnection(dbUrl, properties)
    val statement = connection.createStatement()
    val resultSet = statement.executeUpdate(
      s"""
         |INSERT INTO clients(client_id, name, age, street, house_nr, zipcode, email, telephone, mobile, timestamp) VALUES (
         |         ${client.clientId},
         |          ${client.name},
         |          ${client.age},
         |          ${client.livingAddress.street},
         |          ${client.livingAddress.houseNr},
         |          ${client.livingAddress.zipcode},
         |          ${client.contactInformation.email},
         |          ${client.contactInformation.telephone},
         |          ${client.contactInformation.mobile},
         |          ${client.timestamp}
         |);
      """.stripMargin)

    println(s"Affected Rows After Client Insert: '$resultSet'")

    statement.close()
    connection.close()
  }
}
