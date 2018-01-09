

lazy val `client-bs-service` = (project in file("."))
  .settings(
    schemaDependencies += "com.github.dnvriend.platform.model.client" % "Client" % "1",
  ).enablePlugins(SamSchemaPlugin)