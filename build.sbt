name := "distributed_mastermind"

version := "0.1.1"

scalaVersion := "2.12.12"

val akkaV = "2.5.13"
val akkaTyped = "com.typesafe.akka" %% "akka-actor-typed" % akkaV
val akkaRemote = "com.typesafe.akka" %% "akka-remote" % akkaV
val akkaActor = "com.typesafe.akka" %% "akka-actor" % akkaV
val akkaTestKit = "com.typesafe.akka" %% "akka-testkit" % akkaV % Test
val akkaDependencies = Seq(akkaTyped, akkaRemote, akkaActor, akkaTestKit)

val scalactic = "org.scalactic" %% "scalactic" % "3.2.0"
val scalaTest = "org.scalatest" %% "scalatest" % "3.2.0" % Test
val scalamock = "org.scalamock" %% "scalamock" % "4.4.0" % Test
val scalacheck = "org.scalacheck" %% "scalacheck" % "1.14.1" % Test
val testDependencies = Seq(scalactic, scalaTest, scalamock, scalacheck)

/*
 * Scoverage configurations.
 */
coverageMinimum := 25
coverageFailOnMinimum := true
coverageExcludedPackages := "it.mm.actors.*;"

libraryDependencies ++= (akkaDependencies ++ testDependencies)
scalacOptions ++= Seq(
  "-Ywarn-unused-import", // required by `RemoveUnused` rule
  "-feature"
)

mainClass := Some("it.mm.Mastermind")