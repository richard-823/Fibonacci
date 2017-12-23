lazy val root = project.in(file(".")).enablePlugins(PlayJava)

name := """fibonacci"""

version := "1.0-SNAPSHOT"

scalaVersion := "2.11.7"


libraryDependencies ++= Seq(
  "com.fasterxml.jackson.datatype" % "jackson-datatype-guava" % "2.7.2",
  "com.google.inject.extensions" % "guice-multibindings" % "4.0",
  "io.rest-assured" % "rest-assured" % "3.0.2" % "test"
)

testOptions += Tests.Argument(TestFrameworks.JUnit, "-v", "-q", "-n")
fork in Test := false


