lazy val `sam-authorization` = (project in file("."))
  .settings(
    scalaVersion := "2.12.4",
	  samStage := "dev",
	  organization := "com.github.dnvriend",
	  description := "sam's authentication and authorization component"
  )


