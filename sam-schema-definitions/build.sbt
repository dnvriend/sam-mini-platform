

lazy val `sam-schema-definitions` = (project in file("."))
  .enablePlugins(SamSchemaPlugin)
  .disablePlugins(SAMPlugin)


