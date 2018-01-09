package com.github.dnvriend

import com.github.dnvriend.platform.model.client.{Client, ContactInformation, LivingAddress}
import org.scalacheck.Gen

import scala.compat.Platform

/**
  * A generator for creating client information
  */
object GenClient {
  val genContactInfo = for {
    email <- Gen.alphaStr
    telephone <- Gen.alphaStr
    mobile <- Gen.alphaStr
  } yield ContactInformation(
    email,
    telephone,
    mobile
  )

  val genLivingAddress = for {
    street <- Gen.oneOf("first street", "second street", "third street", "fourth street, fifth street, sixth street, seventh street, eight street")
    houseNr <- Gen.chooseNum(1, 500)
    zipcode <- Gen.oneOf("1000AB", "2000CD", "3000EF","4000GH", "5000IJ")
  } yield LivingAddress(
    street,
    houseNr,
    zipcode
  )
  val genClient = for {
    clientId <- Gen.uuid.map(id => id.toString)
    name <- Gen.alphaStr
    age <- Gen.chooseNum(1, 100)
    livingAddress <- genLivingAddress
    contactInfo <- genContactInfo
    timestamp <- Gen.calendar.map(_.getTimeInMillis)
  } yield Client(
    clientId,
    name,
    age,
    livingAddress,
    contactInfo,
    timestamp
  )

  val iterator: Iterator[Client] = {
    Stream.continually(genClient.sample).collect { case Some(client) => client }.iterator
  }
}