import sbt.Keys.scalacOptions
import scoverage.ScoverageKeys.coverageFailOnMinimum

ThisBuild / version := "0.1.1"
ThisBuild / developers := List(
  Developer(
    id = "ap",
    name = "Antonio Parolisi",
    email = "daniele.tentoni.1996@gmail.com",
    url = url("https://github.com/Daniele-Tentoni")
  )
)
ThisBuild / description := "Simple distributed mastermind project."

val scalaV = "2.12.12"
val akkaV = "2.5.32"

val akkaTyped = "com.typesafe.akka"   %% "akka-actor-typed" % akkaV
val akkaRemote = "com.typesafe.akka"  %% "akka-remote"      % akkaV
val akkaActor = "com.typesafe.akka"   %% "akka-actor"       % akkaV
val akkaTestKit = "com.typesafe.akka" %% "akka-testkit"     % akkaV % Test
val akkaDependencies = Seq(akkaTyped, akkaRemote, akkaActor, akkaTestKit)

val scalactic = "org.scalactic"   %% "scalactic"  % "3.2.0"
val scalaTest = "org.scalatest"   %% "scalatest"  % "3.2.0"  % Test
val scalamock = "org.scalamock"   %% "scalamock"  % "4.4.0"  % Test
val scalacheck = "org.scalacheck" %% "scalacheck" % "1.14.1" % Test
val testDependencies = Seq(scalactic, scalaTest, scalamock, scalacheck)

lazy val core = Project(id = "core", base = file("core"))
  .settings(
    name := "Core",
    scalaVersion := scalaV,
    libraryDependencies ++= (akkaDependencies ++ testDependencies),
    scalacOptions ++= Seq(
      "-deprecation",
      "-Ywarn-unused-import", // required by `RemoveUnused` rule
      "-feature"
    ),
    resolvers += Resolver.mavenLocal,
    commands ++= Seq(hello),
    /*
     * Scoverage configurations.
     */
    coverageMinimum := 25,
    coverageFailOnMinimum := false,
    coverageExcludedPackages := "it.mm.actors.models.*;it.mm.actors.Lobby*;it.mm.Utils*;it.mm.Mastermind"
  )

lazy val public = Project(id = "public", base = file("public"))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    name := "Public client",
    scalaVersion := scalaV,
    scalaJSUseMainModuleInitializer := true
  )
  .dependsOn(core)

lazy val telegram = Project(id = "telegram", base = file("telegram"))
  .enablePlugins(JavaServerAppPackaging)
  .settings(
    // necessary for sbt-native-packager: https://stackoverflow.com/a/30417973/10220116
    mainClass in Compile := Some("it.mm.telegram.Bot"),
    name := "Telegram interface",
    scalaVersion := scalaV,
    libraryDependencies ++= testDependencies,
    // Core with minimal dependencies, enough to spawn your first bot.
    libraryDependencies += "com.bot4s" %% "telegram-core" % "4.4.0-RC2",
    // Extra goodies: Webhooks, support for games, bindings for actors.
    libraryDependencies += "com.bot4s" %% "telegram-akka" % "4.4.0-RC2"
    // coverageMinimum := 25,
    // coverageFailOnMinimum := false
  )
  .dependsOn(core)

def hello = Command.command("hello") { state =>
  println("Hi!")
  state
}
