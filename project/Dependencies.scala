import sbt._

object Dependencies {
  lazy val scalaTestParent = "org.scalatest"
  lazy val scalaTestVersion = "3.0.8"
  lazy val scalaMockParent = "org.scalamock"
  lazy val scalaMockVersion = "4.4.0"
  lazy val scalaCheckParent = "org.scalacheck"
  lazy val scalaCheckVersion = "1.14.3"
  lazy val scalaTestPlusParent = "org.scalatestplus"
  lazy val scalaTestPlusVersion = "3.1.0.1"

  def cats(artifact: String): ModuleID = "org.typelevel" %% artifact % "2.1.1"
  def monix(artifact: String): ModuleID = "io.monix" %% artifact % "3.1.0"
  def fs2(artifact: String): ModuleID = "co.fs2" %% artifact % "2.2.1"
  def fs2Kafka(artifact: String): ModuleID = "com.github.fd4s" %% artifact % "1.0.0"
  def circe(artifact: String): ModuleID = "io.circe" %% artifact % "0.12.3"
  def jackson(artifact: String): ModuleID = "com.fasterxml.jackson.module" %% artifact % "2.11.0"
  def jacksonModule(artifact: String): ModuleID =  "com.fasterxml.jackson.datatype" % artifact % "2.11.0"
  def log4cats(artifact: String): ModuleID = "io.chrisdavenport" %% artifact % "1.0.1"
  def scalaLog(artifact: String): ModuleID = "com.typesafe.scala-logging" %% artifact % "3.9.2"
  def logback(artifact: String): ModuleID = "ch.qos.logback" % artifact % "1.2.3"
  def logbackEncoder(artifact: String): ModuleID = "net.logstash.logback" % artifact % "6.3"

  val dependencies = Seq(
    cats("cats-macros"),
    cats("cats-kernel"),
    cats("cats-core"),
    cats("cats-effect"),
    monix("monix-eval"),
    monix("monix-execution"),
    fs2("fs2-core"),
    fs2Kafka("fs2-kafka"),
    circe("circe-generic"),
    circe("circe-literal"),
    jackson("jackson-module-scala"),
    jacksonModule("jackson-datatype-jdk8"),
    jacksonModule("jackson-datatype-jsr310"),
    scalaLog("scala-logging"),
    logback("logback-classic"),
    logbackEncoder("logstash-logback-encoder"),
    log4cats("log4cats-core"),
    log4cats("log4cats-slf4j")
  )

  val testDependencies = Seq(
    scalaTestParent %% "scalatest" % scalaTestVersion % Test,
    scalaMockParent %% "scalamock" % scalaMockVersion % Test,
    scalaCheckParent %% "scalacheck" % scalaCheckVersion % Test,
    scalaTestPlusParent %% "scalacheck-1-14" % scalaTestPlusVersion % Test
  )
}
