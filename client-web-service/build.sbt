

lazy val `client-web-service` = (project in file("."))
  .settings(
    schemaDependencies += "com.github.dnvriend.platform.model.client" % "Client" % "1",
  ).enablePlugins(SamSchemaPlugin)