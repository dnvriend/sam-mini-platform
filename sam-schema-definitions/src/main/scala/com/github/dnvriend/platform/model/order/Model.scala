package com.github.dnvriend.platform.model.order

import com.github.dnvriend.sam.serialization.annotation.SamSchema
import com.sksamuel.avro4s.AvroDoc

@SamSchema
@AvroDoc("Orders that have been placed by a client")
case class Order(
                @AvroDoc("The id of the order") orderId: String = "",

                @AvroDoc("The id of the client that placed the order") clientId: String = "",

                @AvroDoc("The name of the order") name: String = "",

                @AvroDoc("The details of the order") orderLines: List[OrderLine] = List.empty,

                @AvroDoc("The timestamp that the order has been placed") timestamp: Long = 0L,
                )

@AvroDoc("Describes a single item of an order")
case class OrderLine(
                    @AvroDoc("The id of the product that has been selected") productId: String = "",

                    @AvroDoc("The name of the product that has been selected") name: String = "",

                    @AvroDoc("How many items of this product has been selected") numItems: Int = 0,

                    @AvroDoc("The price of a single item") price: Int = 0,
                    )