name := "refactor_to_functional"

version := "1.0"

scalaVersion := "2.12.4"

resolvers += "Artima Maven Repository" at "http://repo.artima.com/releases"

libraryDependencies ++= Seq(
  "com.chuusai" %% "shapeless" % "2.3.3" withSources(),
  "org.typelevel" %% "cats-core" % "1.0.1" withSources(),
  "com.typesafe.akka" %% "akka-http" % "10.1.0-RC1" withSources(),
  "com.typesafe.akka" %% "akka-stream" % "2.5.9" withSources(),
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.0-RC1" withSources(),
  "org.scalactic" %% "scalactic" % "3.0.4" withSources(),
  "org.scalatest" %% "scalatest" % "3.0.4" % "test" withSources()
)

scalacOptions += "-Ypartial-unification"