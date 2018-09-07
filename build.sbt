
lazy val root = (project in file("."))
  .enablePlugins(JavaAgent)
  .settings(javaAgents += "org.aspectj" % "aspectjweaver" % "1.8.11"  % "runtime")
  .settings(
    inThisBuild(List(
      scalaVersion    := "2.12.2"
    )),
    name := "http4s-traceid-test",
    libraryDependencies ++= Seq(
      "ch.qos.logback" %  "logback-classic"      % "1.1.7",
      "org.http4s"     %% "http4s-dsl"           % "0.18.4",
      "org.http4s"     %% "http4s-blaze-server"  % "0.18.4",
      "io.kamon"       %% "kamon-core"           % "1.1.3",
      "io.kamon"       %% "kamon-http4s"         % "1.0.8",
      "io.kamon"       %% "kamon-executors"      % "1.0.2",
      "io.kamon"       %% "kamon-logback"        % "1.0.2"
    )
  )
