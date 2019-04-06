name := "dex_backend"

version := "0.1"

scalaVersion := "2.12.8"

val circeVersion = "0.8.0"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.5.+",
  "com.typesafe.akka" %% "akka-stream" % "2.5.+",
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,
  "org.scalatest" %% "scalatest" % "3.0.5" % "test",
  "org.scalacheck" %% "scalacheck" % "1.14.+" % "test",
)
