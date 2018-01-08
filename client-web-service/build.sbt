lazy val `client-web-service` = (project in file("."))
  .settings(
    libraryDependencies += "com.github.dnvriend" %% "sam-annotations" % "1.0.16",
    libraryDependencies += "com.github.dnvriend" %% "sam-lambda" % "1.0.16",
    libraryDependencies += "com.github.dnvriend" %% "sam-serialization" % "1.0.16",
    libraryDependencies += "com.amazonaws" % "aws-lambda-java-core" % "1.2.0",
    libraryDependencies += "com.amazonaws" % "aws-java-sdk-kinesis" % "1.11.255",
    libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.13.4",
    libraryDependencies += "com.fasterxml.jackson.dataformat" % "jackson-dataformat-cbor" % "2.9.3",
    resolvers += Resolver.bintrayRepo("dnvriend", "maven"),
    scalaVersion := "2.12.4",
    samStage := "dev",
    organization := "com.github.dnvriend",
    description := "client reg service",

    schemaRepositoryUrl := "https://na1f4jdmvc.execute-api.eu-west-1.amazonaws.com/dev",
    schemaUserPoolId := "eu-west-1_vVjghzL3w",
    schemaClientId := "52gia00eeiuvbspdr7jr86eohg",
    schemaUsername := "admin",
    schemaPassword := "it-is-a-secret-2018",

    schemaDependencies += "com.github.dnvriend.platform.model.client" % "Client" % "1",

  ).enablePlugins(SamSchemaPlugin)