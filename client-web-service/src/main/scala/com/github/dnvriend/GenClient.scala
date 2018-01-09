package com.github.dnvriend

import com.github.dnvriend.platform.model.client.{Client, ContactInformation, LivingAddress}
import org.scalacheck.Gen

import scala.compat.Platform


object Main extends App {
  println(GenClient.iterator.next())
}

/**
  * A generator for creating client information
  */
object GenClient {
  def now: Long = Platform.currentTime
  val firstNames: List[String] = scala.io.Source.fromInputStream(getClass.getClassLoader.getResourceAsStream("firstnames.csv")).getLines().toList
  val lastNames: List[String] = scala.io.Source.fromInputStream(getClass.getClassLoader.getResourceAsStream("lastnames.csv")).getLines().toList
  val streetNames: List[String] = scala.io.Source.fromInputStream(getClass.getClassLoader.getResourceAsStream("street.csv")).getLines().toList.map(_.split(",").headOption).flatten
  val postcodes: List[String] = scala.io.Source.fromInputStream(getClass.getClassLoader.getResourceAsStream("postcode.csv")).getLines().toList.map(_.split(",").drop(1).headOption).flatten

  val genName = for {
    fn <- Gen.oneOf(firstNames)
    ln <- Gen.oneOf(lastNames)
  } yield s"$fn $ln"

  val genZipcode = for {
    pc <- Gen.oneOf(postcodes)
    l1 <- Gen.alphaUpperChar
    l2 <- Gen.alphaUpperChar
  } yield s"$pc$l1$l2"

  val genMobile = for {
    n1 <- Gen.numChar
    n2 <- Gen.numChar
    n3 <- Gen.numChar
    n4 <- Gen.numChar
    n5 <- Gen.numChar
    n6 <- Gen.numChar
    n7 <- Gen.numChar
    n8 <- Gen.numChar
  } yield s"06-$n1$n2$n3$n4$n5$n6$n7$n8"

  val genTele = for {
    n1 <- Gen.numChar
    n2 <- Gen.numChar
    n3 <- Gen.numChar
    n4 <- Gen.numChar
    n5 <- Gen.numChar
    n6 <- Gen.numChar
    n7 <- Gen.numChar
    n8 <- Gen.numChar
  } yield s"0$n1$n2-$n3$n4$n5$n6$n7$n8"

  val genEmail = for {
    c1 <- Gen.alphaLowerChar
    c2 <- Gen.alphaLowerChar
    c3 <- Gen.alphaLowerChar
    c4 <- Gen.alphaLowerChar
    c5 <- Gen.alphaLowerChar
    c6 <- Gen.alphaLowerChar
    c7 <- Gen.alphaLowerChar
    c8 <- Gen.alphaLowerChar
    c9 <- Gen.alphaLowerChar
    domain <- Gen.oneOf(List("google.com", "hotmail.com", "amazon.com", "yahoo.com", "yandex.com", "protonmail.com", "aol.com", "aim.com", "icloud.com", "apple.com", "microsoft.com", "mail.com"))
  } yield s"$c1$c2$c3$c4$c5$c6$c7$c8$c9@$domain"

  val genContactInfo = for {
    email <- genEmail
    telephone <- genTele
    mobile <- genMobile
  } yield ContactInformation(
    email,
    telephone,
    mobile
  )

  val genLivingAddress = for {
    street <- Gen.oneOf(streetNames)
    houseNr <- Gen.chooseNum(1, 500)
    zipcode <- genZipcode
  } yield LivingAddress(
    street,
    houseNr,
    zipcode
  )
  val genClient = for {
    clientId <- Gen.uuid.map(id => id.toString)
    name <- genName
    age <- Gen.chooseNum(1, 100)
    livingAddress <- genLivingAddress
    contactInfo <- genContactInfo
  } yield Client(
    clientId,
    name,
    age,
    livingAddress,
    contactInfo,
    now
  )

  val iterator: Iterator[Client] = {
    Stream.continually(genClient.sample).collect { case Some(client) => client }.iterator
  }
}