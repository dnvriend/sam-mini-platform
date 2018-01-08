organization in ThisBuild := "com.github.dnvriend"

lazy val `sam-mini-platform` = (project in file("."))
	.disablePlugins(SamSchemaPlugin, SAMPlugin, AwsPlugin)

// core components
lazy val `sam-authorization` = project in file("sam-authorization")
  
lazy val `sam-schema-repo` = project in file("sam-schema-repo")

lazy val `sam-schema-definitions` = project in file("sam-schema-definitions")

// client
lazy val `client-web-service` = project in file("client-web-service")

lazy val `client-intake` = project in file("client-intake")

lazy val `client-bs-service` = project in file("client-bs-service")

lazy val `client-release` = project in file("client-release")

// order
lazy val `order-web-service` = project in file("order-web-service")

lazy val `order-intake` = project in file("order-intake")

lazy val `order-bs-service` = project in file("order-bs-service")

lazy val `order-bs-na-service` = project in file("order-bs-na-service")

lazy val `order-release` = project in file("order-release")

lazy val `order-na-release` = project in file("order-na-release")