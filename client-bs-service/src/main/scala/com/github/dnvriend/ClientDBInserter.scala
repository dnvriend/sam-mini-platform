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
  private val dbUrl = s"jdbc:postgresql://$dbendpoint:$dbPort/$dbName"

  private val properties: Properties = new Properties()
  properties.setProperty("useSSL", "false")
  properties.setProperty("user", dbUserName)
  properties.setProperty("password", dbPassword)

  def toDateFormat(time: Long): String = {
    //'2018-01-08 14:29:33'
    new java.text.SimpleDateFormat("YYYY-MM-dd HH:mm:ss").format(new java.util.Date(time))
  }

  def insertClient(client: Client): Unit = {
    val connection = DriverManager.getConnection(dbUrl, properties)
    val statement = connection.createStatement()
    val sql: String = s"""
                         |INSERT INTO public.clients(client_id, name, age, street, house_nr, zipcode, email, telephone, mobile, timestamp) VALUES (
                         |          '${client.clientId}',
                         |          '${client.name}',
                         |          ${client.age},
                         |          '${client.livingAddress.street}',
                         |          ${client.livingAddress.houseNr},
                         |          '${client.livingAddress.zipcode}',
                         |          '${client.contactInformation.email}',
                         |          '${client.contactInformation.telephone}',
                         |          '${client.contactInformation.mobile}',
                         |          '${toDateFormat(client.timestamp)}'
                         |);
      """.stripMargin
    println(sql)
    val resultSet = statement.executeUpdate(sql)

    println(s"Affected Rows After Client Insert: '$resultSet'")

    statement.close()
    connection.close()
  }
}
