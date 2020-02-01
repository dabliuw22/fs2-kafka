import Dependencies._

name := "fs2-kafka"

version := "0.1"

scalaVersion := "2.13.0"

libraryDependencies ++= (dependencies ++ testDependencies)

scalacOptions ++= Seq(
  "-feature",
  "-deprecation",
  "-unchecked",
  "-language:postfixOps",
  "-language:higherKinds"
)