import akka.{ParadoxSupport, AutomaticModuleName}

enablePlugins(TimeStampede, UnidocWithPrValidation, NoPublish, CopyrightHeader, CopyrightHeaderInPr)
disablePlugins(MimaPlugin)

import com.typesafe.sbt.SbtMultiJvm.MultiJvmKeys.MultiJvm
import com.typesafe.tools.mima.plugin.MimaPlugin
import spray.boilerplate.BoilerplatePlugin
import akka.AkkaBuild._
import akka.{AkkaBuild, Dependencies, GitHub, OSGi, Protobuf, SigarLoader, VersionGenerator}
import sbt.Keys.{initialCommands, parallelExecution}

initialize := {
  // Load system properties from a file to make configuration from Jenkins easier
  loadSystemProperties("project/akka-build.properties")
  initialize.value
}

akka.AkkaBuild.buildSettings
shellPrompt := { s => Project.extract(s).currentProject.id + " > " }
resolverSettings

lazy val aggregatedProjects: Seq[ProjectReference] = Seq(
  actor,
  stream
)

lazy val root = Project(
  id = "akka",
  base = file(".")
).aggregate(aggregatedProjects: _*)
 .settings(rootSettings: _*)
 .settings(
   unmanagedSources in(Compile, headerCreate) := (baseDirectory.value / "project").**("*.scala").get
 )

lazy val actor = akkaModule("akka-actor")
  .settings(Dependencies.actor)
  .settings(OSGi.actor)
  .settings(AutomaticModuleName.settings("akka.actor"))
  .settings(
    unmanagedSourceDirectories in Compile += {
      val ver = scalaVersion.value.take(4)
      (scalaSource in Compile).value.getParentFile / s"scala-$ver"
    }
  )
  .settings(VersionGenerator.settings)
  .enablePlugins(BoilerplatePlugin)

lazy val protobuf = akkaModule("akka-protobuf")
  .settings(OSGi.protobuf)
  .settings(AutomaticModuleName.settings("akka.protobuf"))
  .disablePlugins(MimaPlugin)

lazy val stream = akkaModule("akka-stream")
  .dependsOn(actor, protobuf)
  .settings(Dependencies.stream)
  .settings(AutomaticModuleName.settings("akka.stream"))
  .settings(OSGi.stream)
  .settings(Protobuf.settings)
  .enablePlugins(BoilerplatePlugin)

lazy val streamTestkit = akkaModule("akka-stream-testkit")
  .dependsOn(stream, testkit % "compile->compile;test->test")
  .settings(Dependencies.streamTestkit)
  .settings(AutomaticModuleName.settings("akka.stream.testkit"))
  .settings(OSGi.streamTestkit)
  .disablePlugins(MimaPlugin)

lazy val testkit = akkaModule("akka-testkit")
  .dependsOn(actor)
  .settings(Dependencies.testkit)
  .settings(AutomaticModuleName.settings("akka.actor.testkit"))
  .settings(OSGi.testkit)
  .settings(
    initialCommands += "import akka.testkit._"
  )

def akkaModule(name: String): Project =
  Project(id = name, base = file(name))
    .settings(akka.AkkaBuild.buildSettings)
    .settings(akka.AkkaBuild.defaultSettings)
    .settings(akka.Formatting.formatSettings)

