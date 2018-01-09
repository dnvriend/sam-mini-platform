
// aggregate project
lazy val `sam-mini-platform` = (project in file("."))
	.disablePlugins(SamSchemaPlugin, SAMPlugin, AwsPlugin)
	.aggregate(
		`client-web-service`,
		`client-bs-service`,
		`order-web-service`,
		`order-bs-service`,
		`order-bs-na-service`,
	)

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

// orchestration tasks
lazy val deployCore = taskKey[Unit]("Deploy core components")
deployCore := {
	Def.sequential(samDeploy in `sam-authorization`, samCreateUsers in `sam-authorization`, samDeploy in `sam-schema-repo`).value
	(samDeploy in `client-intake`).value
	(samDeploy in `client-release`).value
	(samDeploy in `order-release`).value
	(samDeploy in `order-intake`).value
	(samDeploy in `order-na-release`).value
}

lazy val deployServices = taskKey[Unit]("Deploy service components")
deployServices := {
	(samDeploy in `client-web-service`).value
	(samDeploy in `client-bs-service`).value
	(samDeploy in `order-web-service`).value
	(samDeploy in `order-bs-service`).value
	(samDeploy in `order-bs-na-service`).value
}

lazy val removeServices = taskKey[Unit]("Remove service components")
removeServices := {
	(samRemove in `client-web-service`).value
	(samRemove in `client-bs-service`).value
	(samRemove in `order-web-service`).value
	(samRemove in `order-bs-service`).value
	(samRemove in `order-bs-na-service`).value
}

lazy val removeCore = taskKey[Unit]("Remove core components")
removeCore := {
	(samRemove in `client-intake`).value
	(samRemove in `client-release`).value
	(samRemove in `order-release`).value
	(samRemove in `order-intake`).value
	(samRemove in `order-na-release`).value
	Def.sequential(samRemove in `sam-schema-repo`, samRemove in `sam-authorization`).value
}

// remove platform orchestration
lazy val removeMiniPlatform = taskKey[Unit]("Removes the mini platform")
removeMiniPlatform := {
	Def.sequential(removeServices, removeCore).value
}

// deploy platform orchestration
lazy val deployMiniPlatform = taskKey[Unit]("Deploys the mini platform")
deployMiniPlatform := {
	Def.sequential(deployCore, deployServices).value
}