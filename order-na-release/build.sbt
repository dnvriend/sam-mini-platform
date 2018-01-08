lazy val `order-na-release` = (project in file("."))
  .settings(
	libraryDependencies += "com.github.dnvriend" %% "sam-annotations" % "1.0.16",
    libraryDependencies += "com.github.dnvriend" %% "sam-lambda" % "1.0.16",
    libraryDependencies += "com.amazonaws" % "aws-lambda-java-core" % "1.2.0",
    resolvers += Resolver.bintrayRepo("dnvriend", "maven"),
    scalaVersion := "2.12.4",
	samStage := "dev",
	organization := "com.github.dnvriend",
	description := "a data segment, a single element of a data lake",

    schemaRepositoryUrl := "https://na1f4jdmvc.execute-api.eu-west-1.amazonaws.com/dev",
    schemaUserPoolId := "eu-west-1_vVjghzL3w",
    schemaClientId := "52gia00eeiuvbspdr7jr86eohg",
    schemaUsername := "admin",
    schemaPassword := "it-is-a-secret-2018",

  )


