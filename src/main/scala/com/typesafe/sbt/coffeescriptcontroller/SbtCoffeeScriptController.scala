package com.typesafe.sbt.coffeescriptcontroller

import com.typesafe.sbt.jse.SbtJsTask
import sbt._
import com.typesafe.sbt.web.SbtWeb
import spray.json.{JsBoolean, JsObject}
import sbt.Keys._

object Import {

  object CoffeeScriptControllerKeys {
    val coffeescriptController = TaskKey[Seq[File]]("coffeescriptcontroller", "Invoke the CoffeeScript compiler.")

    val bare = SettingKey[Boolean]("coffeescriptcontroller-bare", "Compiles JavaScript that isn't wrapped in a function.")
    val sourceMap = SettingKey[Boolean]("coffeescriptcontroller-source-map", "Outputs a v3 sourcemap.")
  }

}

object SbtCoffeeScriptController extends AutoPlugin {

  override def requires = SbtJsTask

  override def trigger = AllRequirements

  val autoImport = Import

  import SbtWeb.autoImport._
  import WebKeys._
  import SbtJsTask.autoImport.JsTaskKeys._
  import autoImport.CoffeeScriptControllerKeys._

  val coffeeScriptUnscopedSettings = Seq(

    includeFilter := "*Controller.coffee",

    jsOptions := JsObject(
      "bare" -> JsBoolean(bare.value),
      "sourceMap" -> JsBoolean(sourceMap.value)
    ).toString()
  )

  override def projectSettings = Seq(
    bare := false,
    sourceMap := true

  ) ++ inTask(coffeescriptController)(
    SbtJsTask.jsTaskSpecificUnscopedSettings ++
      inConfig(Assets)(coffeeScriptUnscopedSettings) ++
      inConfig(TestAssets)(coffeeScriptUnscopedSettings) ++
      Seq(
        moduleName := "coffeescriptcontroller",
        shellFile := getClass.getClassLoader.getResource("coffee.js"),

        taskMessage in Assets := "CoffeeScript compiling controller",
        taskMessage in TestAssets := "CoffeeScript test compiling controller"
      )
  ) ++ SbtJsTask.addJsSourceFileTasks(coffeescriptController) ++ Seq(
    coffeescriptController in Assets := (coffeescriptController in Assets).dependsOn(webModules in Assets).value,
    coffeescriptController in TestAssets := (coffeescriptController in TestAssets).dependsOn(webModules in TestAssets).value
  )

}
