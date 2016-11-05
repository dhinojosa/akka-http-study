name := "akka-http"

version := "1.0-SNAPSHOT"

scalaVersion := "2.11.8"

libraryDependencies := Seq(
  "org.scalatest" % "scalatest_2.11" % "2.2.6" % "test",
  "junit" % "junit" % "4.12" % "test",
  "org.assertj" % "assertj-core" % "3.5.2" % "test",
  "com.typesafe.akka" %% "akka-http" % "3.0.0-RC1",
  "com.typesafe.akka" %% "akka-http-spray-json" % "3.0.0-RC1",
  "com.typesafe.akka" %% "akka-http-xml" % "3.0.0-RC1"
)

