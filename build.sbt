name := "akka-http-study"

version := "1.0-SNAPSHOT"

scalaVersion := "2.12.6"

val akkaVersion = "2.5.18"

val akkaHttpVersion = "10.1.5"

libraryDependencies := Seq(
  "org.scalatest" %% "scalatest" % "3.0.5" % "test",
  "junit" % "junit" % "4.12" % "test",
  "org.assertj" % "assertj-core" % "3.5.2" % "test",
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-xml" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-cluster-tools" % akkaVersion,
  "com.typesafe.akka" %% "akka-remote" % akkaVersion,
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0",
  "com.typesafe.akka" %% "akka-stream-kafka" % "1.0-M1",
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion
)

EclipseKeys.withSource := true

EclipseKeys.withJavadoc := true

EclipseKeys.executionEnvironment := Some(EclipseExecutionEnvironment.JavaSE18)