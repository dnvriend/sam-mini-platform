

lazy val `order-proc-gov` = (project in file("."))
  .settings(
    schemaDependencies += "com.github.dnvriend.platform.model.order" % "Order" % "1",
  ).enablePlugins(SamSchemaPlugin)