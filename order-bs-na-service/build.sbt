

lazy val `order-bs-na-service` = (project in file("."))
  .settings(
      schemaDependencies += "com.github.dnvriend.platform.model.order" % "Order" % "1",
      schemaDependencies += "com.github.dnvriend.platform.model.client" % "Client" % "1",
  ).enablePlugins(SamSchemaPlugin)
