package com.github.dnvriend.platform.model.client

import com.github.dnvriend.sam.serialization.annotation.SamSchema
import com.sksamuel.avro4s.AvroDoc

@SamSchema
@AvroDoc("Client interacts with the business processes. A client can log in, place orders and so on")
case class Client(
                   @AvroDoc("The ID of the client, will be used eg. when placing orders") clientId: String = "",
                   @AvroDoc("The name of the client") name: String = "",
                   @AvroDoc("The age of the client") age: Int = 0,
                   @AvroDoc("The living address of the client") livingAddress: LivingAddress = LivingAddress(),
                   @AvroDoc("Contact information of the client") contactInformation: ContactInformation = ContactInformation(),
                   @AvroDoc("The timestamp that the client has put details") timestamp: Long = 0L,
               )

@AvroDoc("The address where the client lives")
case class LivingAddress(
                        @AvroDoc("Street name") street: String = "",
                        @AvroDoc("House number") houseNr: Int = 0,
                        @AvroDoc("zip code") zipcode: String = "",
                  )

@AvroDoc("Possible contact channels of the client")
case class ContactInformation(
                             @AvroDoc("email contact channel") email: String = "",
                             @AvroDoc("telephone contact channel") telephone: String = "",
                             @AvroDoc("mobile telephone channel") mobile: String = "",
                           )