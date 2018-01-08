lazy val `client-web-service` = (project in file("."))
  .settings(
    libraryDependencies += "com.github.dnvriend" %% "sam-annotations" % "1.0.16-SNAPSHOT",
    libraryDependencies += "com.github.dnvriend" %% "sam-lambda" % "1.0.16-SNAPSHOT",
    libraryDependencies += "com.github.dnvriend" %% "sam-serialization" % "1.0.16-SNAPSHOT",
    libraryDependencies += "com.amazonaws" % "aws-lambda-java-core" % "1.2.0",
    libraryDependencies += "com.amazonaws" % "aws-java-sdk-kinesis" % "1.11.255",
    libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.13.4",
    libraryDependencies += "com.fasterxml.jackson.dataformat" % "jackson-dataformat-cbor" % "2.9.3",
    resolvers += Resolver.bintrayRepo("dnvriend", "maven"),
    scalaVersion := "2.12.4",
    samStage := "dev",
    organization := "com.github.dnvriend",
    description := "client reg service",

    schemaRepositoryUrl := "https://7i44410fr4.execute-api.eu-central-1.amazonaws.com/dev",
    schemaUserPoolId := "eu-central-1_qhxmO5GiZ",
    schemaClientId := "388hejrmb70a84ra2ls9cif16v",
    schemaUsername := "admin",
    schemaPassword := "it-is-a-secret-2018",

    schemaDependencies += "com.github.dnvriend.platform.model.client" % "Client" % "2",

  ).enablePlugins(SamSchemaPlugin)