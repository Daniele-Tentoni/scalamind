// Produce Coverage Test Reports in HTML for browsers.
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.6.1")
addSbtPlugin("org.scalameta" % "sbt-scalafmt"  % "2.4.2")

addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.3.1")

// The goal [of the plugin] is to be able to bundle up Scala software built with SBT for native packaging systems, like deb, rpm, homebrew, msi.
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.7.6")
