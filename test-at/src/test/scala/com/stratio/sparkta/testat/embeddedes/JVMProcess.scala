

package com.stratio.sparkta.testat.embeddedes

import java.lang.System.{getProperty => Prop}

object JVMProcess {

  val sep        = Prop("file.separator")
  val classpath  = Prop("java.class.path")
  val path       = Prop("java.home") + sep + "bin" + sep + "java"
  
  var process: Process = _
  
  
  def runMain(className: String, redirectStream: Boolean) {
    val processBuilder = new ProcessBuilder(path, "-cp", classpath, className)
    processBuilder.command().toString()

    processBuilder.redirectErrorStream(redirectStream)

    process = processBuilder.start()
  }

  def shutdown() {
    process.destroy()
  }
}
