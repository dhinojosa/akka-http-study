name := "akka-http"

version := "1.0-SNAPSHOT"

scalaVersion := "2.11.8"

libraryDependencies := Seq(
  "org.scalatest" % "scalatest_2.11" % "2.2.6" % "test",
  "junit" % "junit" % "4.12" % "test",
  "org.assertj" % "assertj-core" % "3.5.2" % "test",
  "com.typesafe.akka" % "akka-stream_2.11" % "2.4.12",
  "com.typesafe.akka" % "akka-http-core_2.11" % "2.4.11"
)

