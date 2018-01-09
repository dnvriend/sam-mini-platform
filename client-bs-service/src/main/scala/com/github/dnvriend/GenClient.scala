package com.github.dnvriend

import com.github.dnvriend.platform.model.client.{Client, ContactInformation, LivingAddress}
import org.scalacheck.Gen

import scala.compat.Platform

/**
  * A generator for creating client information
  */
object GenClient {
  def timestamp: Long = Platform.currentTime

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
    street <- Gen.alphaStr
    houseNr <- Gen.posNum[Int]
    zipcode <- Gen.alphaStr
  } yield LivingAddress(
    street,
    houseNr,
    zipcode
  )
  val genClient = for {
    clientId <- Gen.uuid.map(id => id.toString)
    name <- Gen.alphaStr
    age <- Gen.posNum[Int]
    livingAddress <- genLivingAddress
    contactInfo <- genContactInfo
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