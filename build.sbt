
// aggregate project
lazy val `sam-mini-platform` = (project in file("."))
	.disablePlugins(SamSchemaPlugin, SAMPlugin, AwsPlugin)
	.aggregate(
		`client-web-service`,
		`client-proc`,
		`client-bs-service`,
		`order-web-service`,
		`order-proc`,
		`order-proc-gov`,
		`order-bs-service`,
		`order-bs-service-gov`,
	)

// core components
lazy val `sam-authorization` = project in file("sam-authorization")
  
lazy val `sam-schema-repo` = project in file("sam-schema-repo")

lazy val `sam-schema-definitions` = project in file("sam-schema-definitions")

// client
lazy val `client-intake` = project in file("client-intake")

lazy val `client-master` = project in file("client-master")

lazy val `client-release` = project in file("client-release")

lazy val `client-web-service` = project in file("client-web-service")

lazy val `client-proc` = project in file("client-proc")

lazy val `client-bs-service` = project in file("client-bs-service")

// order
lazy val `order-intake` = project in file("order-intake")

lazy val `order-master` = project in file("order-master")

lazy val `order-master-gov` = project in file("order-master-gov")

lazy val `order-release` = project in file("order-release")

lazy val `order-web-service` = project in file("order-web-service")

lazy val `order-proc` = project in file("order-proc")

lazy val `order-proc-gov` = project in file("order-proc-gov")

lazy val `order-bs-service` = project in file("order-bs-service")

lazy val `order-bs-service-gov` = project in file("order-bs-service-gov")

// orchestration tasks
lazy val deployCore = taskKey[Unit]("Deploy core components")
deployCore := {
	Def.sequential(samDeploy in `sam-authorization`, samCreateUsers in `sam-authorization`, samDeploy in `sam-schema-repo`).value
	(samDeploy in `client-intake`).value
	(samDeploy in `client-release`).value
	(samDeploy in `client-master`).value
	(samDeploy in `order-intake`).value
	(samDeploy in `order-master`).value
	(samDeploy in `order-master-gov`).value
	(samDeploy in `order-release`).value
}

lazy val deployServices = taskKey[Unit]("Deploy service components")
deployServices := {
	(samDeploy in `client-web-service`).value
	(samDeploy in `client-proc`).value
	(samDeploy in `client-bs-service`).value
	(samDeploy in `order-web-service`).value
	(samDeploy in `order-proc`).value
	(samDeploy in `order-proc-gov`).value
	(samDeploy in `order-bs-service`).value
	(samDeploy in `order-bs-service-gov`).value
}

lazy val removeServices = taskKey[Unit]("Remove service components")
removeServices := {
	(samRemove in `client-web-service`).value
	(samRemove in `client-proc`).value
	(samRemove in `client-bs-service`).value
	(samRemove in `order-web-service`).value
	(samRemove in `order-proc`).value
	(samRemove in `order-proc-gov`).value
	(samRemove in `order-bs-service`).value
	(samRemove in `order-bs-service-gov`).value
}

lazy val removeCore = taskKey[Unit]("Remove core components")
removeCore := {
	(samRemove in `client-intake`).value
	(samRemove in `client-release`).value
	(samRemove in `client-master`).value
	(samRemove in `order-intake`).value
	(samRemove in `order-release`).value
	(samRemove in `order-master`).value
	(samRemove in `order-master-gov`).value
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