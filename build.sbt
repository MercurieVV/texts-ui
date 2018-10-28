organization := "nw"
name := "Texts_ui"
version := "0.1.0"

scalaVersion := "2.12.6"

resolvers += "jitpack" at "https://jitpack.io"

libraryDependencies ++= Seq(
//  "io.github.outwatch" %%% "outwatch" % "1.0.0-RC2",
  "com.github.outwatch" % "outwatch" % "master-SNAPSHOT",
  "org.scalatest" %%% "scalatest" % "3.0.5" % Test,
  
  "com.beachape" %%% "enumeratum" % "1.5.13",
  "com.beachape" %%% "enumeratum-circe" % "1.5.13",
  "eu.timepit" %%% "refined" % "0.9.2",
  "io.circe" %%% "circe-refined" % "0.9.3",
)
libraryDependencies += "com.pepegar" %%% "hammock-core" % "0.8.4"
libraryDependencies += "com.pepegar" %%% "hammock-circe" % "0.8.4"

enablePlugins(ScalaJSBundlerPlugin)
scalacOptions += "-P:scalajs:sjsDefinedByDefault"
useYarn := true // makes scalajs-bundler use yarn instead of npm
requiresDOM in Test := true
scalaJSUseMainModuleInitializer := true
scalaJSModuleKind := ModuleKind.CommonJSModule // configure Scala.js to emit a JavaScript module instead of a top-level script


scalacOptions ++=
  "-encoding" :: "UTF-8" ::
  "-unchecked" ::
  "-deprecation" ::
  "-explaintypes" ::
  "-feature" ::
  "-language:_" ::
  "-Xfuture" ::
  "-Xlint" ::
  "-Ypartial-unification" ::
  "-Yno-adapted-args" ::
  "-Ywarn-extra-implicit" ::
  "-Ywarn-infer-any" ::
  "-Ywarn-value-discard" ::
  "-Ywarn-nullary-override" ::
  "-Ywarn-nullary-unit" ::
  Nil




// hot reloading configuration:
// https://github.com/scalacenter/scalajs-bundler/issues/180
addCommandAlias("dev", "; compile; fastOptJS::startWebpackDevServer; devwatch; fastOptJS::stopWebpackDevServer")
addCommandAlias("devwatch", "~; fastOptJS; copyFastOptJS")

version in webpack := "4.16.1"
version in startWebpackDevServer := "3.1.4"
webpackDevServerExtraArgs := Seq("--progress", "--color")
webpackConfigFile in fastOptJS := Some(baseDirectory.value / "webpack.config.dev.js")

webpackBundlingMode in fastOptJS := BundlingMode.LibraryOnly() // https://scalacenter.github.io/scalajs-bundler/cookbook.html#performance

// when running the "dev" alias, after every fastOptJS compile all artifacts are copied into
// a folder which is served and watched by the webpack devserver.
// this is a workaround for: https://github.com/scalacenter/scalajs-bundler/issues/180
lazy val copyFastOptJS = TaskKey[Unit]("copyFastOptJS", "Copy javascript files to target directory")
copyFastOptJS := {
  val inDir = (crossTarget in (Compile, fastOptJS)).value
  val outDir = (crossTarget in (Compile, fastOptJS)).value / "dev"
  val files = Seq(name.value.toLowerCase + "-fastopt-loader.js", name.value.toLowerCase + "-fastopt.js") map { p => (inDir / p, outDir / p) }
  IO.copy(files, overwrite = true, preserveLastModified = true, preserveExecutable = true)
}
