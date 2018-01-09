

lazy val `client-bs-service` = (project in file("."))
  .settings(
    libraryDependencies += "org.postgresql" % "postgresql" % "42.1.4",
    schemaDependencies += "com.github.dnvriend.platform.model.client" % "Client" % "1",
  ).enablePlugins(SamSchemaPlugin)