import sbt.{Def, _}
import sbt.Keys._
import com.github.dnvriend.sbt.sam.SAMPluginKeys._
import com.github.dnvriend.sam.schema.plugin.SamSchemaPluginKeys._

object GlobalBuildSettings extends AutoPlugin {
  override def trigger = allRequirements

  override def requires = plugins.JvmPlugin

  // put these settings at the build level (for all projects)
  override def buildSettings: Seq[Def.Setting[_]] = Seq(
    organization := "com.github.dnvriend",
    samStage := "dn",
    scalaVersion := "2.12.4",
  ) ++ samPluginSettings ++ resolverSettings ++ librarySettings

  lazy val samPluginSettings = Seq(
    schemaRepositoryUrl := "https://na1f4jdmvc.execute-api.eu-west-1.amazonaws.com/dev",
    schemaUserPoolId := "eu-west-1_vVjghzL3w",
    schemaClientId := "52gia00eeiuvbspdr7jr86eohg",
    schemaUsername := "admin",
    schemaPassword := "it-is-a-secret-2018",
  )

  lazy val resolverSettings = Seq(
    resolvers += Resolver.bintrayRepo("dnvriend", "maven"),
  )

  lazy val librarySettings = Seq(
    libraryDependencies += "com.github.dnvriend" %% "sam-annotations" % "1.0.16",
    libraryDependencies += "com.github.dnvriend" %% "sam-lambda" % "1.0.16",
    libraryDependencies += "com.github.dnvriend" %% "sam-serialization" % "1.0.16",
    libraryDependencies += "com.github.dnvriend" %% "sam-dynamodb-resolver" % "1.0.16",
    libraryDependencies += "com.amazonaws" % "aws-lambda-java-core" % "1.2.0",
    libraryDependencies += "com.amazonaws" % "aws-java-sdk-kinesis" % "1.11.255",
    libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.13.4",
    libraryDependencies += "org.bouncycastle" % "bcprov-ext-jdk15on" % "1.54",
    libraryDependencies += "com.amazonaws" % "aws-encryption-sdk-java" % "1.3.1",
    libraryDependencies += "com.fasterxml.jackson.dataformat" % "jackson-dataformat-cbor" % "2.9.3",
    libraryDependencies += "org.apache.avro" % "avro" % "1.8.2",
  )
}
