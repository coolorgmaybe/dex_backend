
name := "dex_backend"

version := "0.1"

scalaVersion := "2.12.8"

val circeVersion = "0.8.0"

libraryDependencies ++= Seq(
  "com.google.guava" % "guava" % "21.0",
  "org.scorexfoundation" %% "scrypto" % "2.+",
  "com.typesafe.akka" %% "akka-actor" % "2.5.+",
  "com.typesafe.akka" %% "akka-stream" % "2.5.+",
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,
  "org.scalatest" %% "scalatest" % "3.0.5" % "test",
  "org.scalacheck" %% "scalacheck" % "1.14.+" % "test",
  "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf"
)

PB.targets in Compile := Seq(
  scalapb.gen() -> (sourceManaged in Compile).value / "protobuf"
)