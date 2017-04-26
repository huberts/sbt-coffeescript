package com.typesafe.sbt.coffeescriptmain

import com.typesafe.sbt.jse.SbtJsTask
import sbt._
import com.typesafe.sbt.web.SbtWeb
import spray.json.{JsBoolean, JsObject}
import sbt.Keys._

object Import {

  object CoffeeScriptMainKeys {
    val coffeescriptmain = TaskKey[Seq[File]]("coffeescriptmain", "Invoke the CoffeeScript compiler.")
    val coffeescriptcontroller = TaskKey[Seq[File]]("coffeescriptcontroller", "Invoke the CoffeeScript compiler.")
    val coffeescriptcomponent = TaskKey[Seq[File]]("coffeescriptcomponent", "Invoke the CoffeeScript compiler.")

    val bare = SettingKey[Boolean]("coffeescript-bare", "Compiles JavaScript that isn't wrapped in a function.")
    val sourceMap = SettingKey[Boolean]("coffeescript-source-map", "Outputs a v3 sourcemap.")
  }

}

object SbtCoffeeScriptMain extends AutoPlugin {

  override def requires = SbtJsTask

  override def trigger = AllRequirements

  val autoImport = Import

  import SbtWeb.autoImport._
  import WebKeys._
  import SbtJsTask.autoImport.JsTaskKeys._
  import autoImport.CoffeeScriptMainKeys._

  val coffeeScriptUnscopedSettings = Seq(

    jsOptions := JsObject(
      "bare" -> JsBoolean(bare.value),
      "sourceMap" -> JsBoolean(sourceMap.value)
    ).toString()
  )

  val coffeescriptmainSettings = coffeeScriptUnscopedSettings ++ Seq(
    includeFilter := "*.coffee",
    excludeFilter := "*Component.coffee" | "*Controller.coffee"
  )

  val coffeescriptcontrollerSettings = coffeeScriptUnscopedSettings ++ Seq(
    includeFilter := "*Controller.coffee"
  )

  val coffeescriptcomponentSettings = coffeeScriptUnscopedSettings ++ Seq(
    includeFilter := "*Component.coffee"
  )

  override def projectSettings = Seq(
    bare := false,
    sourceMap := true

  ) ++ inTask(coffeescriptmain)(
    SbtJsTask.jsTaskSpecificUnscopedSettings ++
      inConfig(Assets)(coffeescriptmainSettings) ++
      inConfig(TestAssets)(coffeescriptmainSettings) ++
      Seq(
        moduleName := "coffeescript",
        shellFile := getClass.getClassLoader.getResource("coffee.js"),

        taskMessage in Assets := "CoffeeScript compiling main",
        taskMessage in TestAssets := "CoffeeScript test compiling main"
      )
  ) ++ SbtJsTask.addJsSourceFileTasks(coffeescriptmain) ++ Seq(
    coffeescriptmain in Assets := (coffeescriptmain in Assets).dependsOn(webModules in Assets).value,
    coffeescriptmain in TestAssets := (coffeescriptmain in TestAssets).dependsOn(webModules in TestAssets).value

  ) ++ inTask(coffeescriptcontroller)(
    SbtJsTask.jsTaskSpecificUnscopedSettings ++
      inConfig(Assets)(coffeescriptcontrollerSettings) ++
      inConfig(TestAssets)(coffeescriptcontrollerSettings) ++
      Seq(
        moduleName := "coffeescript",
        shellFile := getClass.getClassLoader.getResource("coffee.js"),

        taskMessage in Assets := "CoffeeScript compiling controller",
        taskMessage in TestAssets := "CoffeeScript test compiling controller"
      )
  ) ++ SbtJsTask.addJsSourceFileTasks(coffeescriptcontroller) ++ Seq(
    coffeescriptcontroller in Assets := (coffeescriptcontroller in Assets).dependsOn(webModules in Assets).value,
    coffeescriptcontroller in TestAssets := (coffeescriptcontroller in TestAssets).dependsOn(webModules in TestAssets).value

  ) ++ inTask(coffeescriptcomponent)(
    SbtJsTask.jsTaskSpecificUnscopedSettings ++
      inConfig(Assets)(coffeescriptcomponentSettings) ++
      inConfig(TestAssets)(coffeescriptcomponentSettings) ++
      Seq(
        moduleName := "coffeescript",
        shellFile := getClass.getClassLoader.getResource("coffee.js"),

        taskMessage in Assets := "CoffeeScript compiling component",
        taskMessage in TestAssets := "CoffeeScript test compiling component"
      )
  ) ++ SbtJsTask.addJsSourceFileTasks(coffeescriptcomponent) ++ Seq(
    coffeescriptcomponent in Assets := (coffeescriptcomponent in Assets).dependsOn(webModules in Assets).value,
    coffeescriptcomponent in TestAssets := (coffeescriptcomponent in TestAssets).dependsOn(webModules in TestAssets).value
  )

}
