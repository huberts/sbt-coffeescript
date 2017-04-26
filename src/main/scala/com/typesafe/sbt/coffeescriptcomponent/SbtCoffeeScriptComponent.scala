package com.typesafe.sbt.coffeescriptcomponent

import com.typesafe.sbt.jse.SbtJsTask
import sbt._
import com.typesafe.sbt.web.SbtWeb
import spray.json.{JsBoolean, JsObject}
import sbt.Keys._

object Import {

  object CoffeeScriptComponentKeys {
    val coffeescriptComponent = TaskKey[Seq[File]]("coffeescriptcomponent", "Invoke the CoffeeScript compiler.")

    val bare = SettingKey[Boolean]("coffeescriptcomponent-bare", "Compiles JavaScript that isn't wrapped in a function.")
    val sourceMap = SettingKey[Boolean]("coffeescriptcomponent-source-map", "Outputs a v3 sourcemap.")
  }

}

object SbtCoffeeScriptComponent extends AutoPlugin {

  override def requires = SbtJsTask

  override def trigger = AllRequirements

  val autoImport = Import

  import SbtWeb.autoImport._
  import WebKeys._
  import SbtJsTask.autoImport.JsTaskKeys._
  import autoImport.CoffeeScriptComponentKeys._

  val coffeeScriptUnscopedSettings = Seq(

    includeFilter := "*Component.coffee",

    jsOptions := JsObject(
      "bare" -> JsBoolean(bare.value),
      "sourceMap" -> JsBoolean(sourceMap.value)
    ).toString()
  )

  override def projectSettings = Seq(
    bare := false,
    sourceMap := true

  ) ++ inTask(coffeescriptComponent)(
    SbtJsTask.jsTaskSpecificUnscopedSettings ++
      inConfig(Assets)(coffeeScriptUnscopedSettings) ++
      inConfig(TestAssets)(coffeeScriptUnscopedSettings) ++
      Seq(
        moduleName := "coffeescript",
        shellFile := getClass.getClassLoader.getResource("coffee.js"),

        taskMessage in Assets := "CoffeeScript compiling component",
        taskMessage in TestAssets := "CoffeeScript test compiling component"
      )
  ) ++ SbtJsTask.addJsSourceFileTasks(coffeescriptComponent) ++ Seq(
    coffeescriptComponent in Assets := (coffeescriptComponent in Assets).dependsOn(webModules in Assets).value,
    coffeescriptComponent in TestAssets := (coffeescriptComponent in TestAssets).dependsOn(webModules in TestAssets).value
  )

}
