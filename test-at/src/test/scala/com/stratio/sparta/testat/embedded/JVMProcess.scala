/*
 * Copyright (C) 2015 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.stratio.sparta.testat.embedded

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
