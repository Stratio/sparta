/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package org.apache.spark.launcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ThreadFactory;

/**
 * Redirects lines read from a given input stream to a j.u.l.Logger (at INFO level).
 */
public class SpartaOutputRedirector {

  private final BufferedReader reader;
  private final Logger sink;
  private final Thread thread;
  private volatile boolean active;

  SpartaOutputRedirector(InputStream in, String loggerName, ThreadFactory tf) {
    this.active = true;
    this.reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
    this.thread = tf.newThread(this::redirect);
    this.sink = LoggerFactory.getLogger(SpartaOutputRedirector.class);
    thread.start();
  }

  private void redirect() {
    try {

      //Pattern to extract log messages with format: INFO\tClass\tLogMessage
      final String pattern = "^([^\t]+)[\t]([^\t]+)[\t](.*)$";

      String line;
      String nextLogLine = "";
      String nextLogLevel = "";
      Boolean nextLogLineLogged = false;

      while ((line = reader.readLine()) != null) {
        if (active) {
          StringBuilder messageToLog = new StringBuilder();
          String logLevel;
          String classPath;
          String message;
          Boolean concatToLastLine = false;

          logLevel = line.replaceAll(pattern, "$1");

          if (validLogLevel(logLevel)) {
            classPath = line.replaceAll(pattern, "$2");
            message = line.replaceAll(pattern, "$3");
            messageToLog.append("Class: ").append(classPath).append("\tMessage: ").append(message);
          } else {
            if (!nextLogLineLogged && !nextLogLine.isEmpty() && !nextLogLevel.isEmpty()) {
              if (!line.isEmpty())
                nextLogLine = nextLogLine + "\n" + line;
              concatToLastLine = true;
            } else {
              logLevel = "INFO";
              message = line;
              messageToLog.append("Message: ").append(message);
            }
          }

          if (concatToLastLine) {
            logLevel = nextLogLevel;
            messageToLog.append(nextLogLine);
          } else if (!nextLogLineLogged && !nextLogLine.isEmpty() && !nextLogLevel.isEmpty()) {
            logFromLogLevel(nextLogLevel, nextLogLine);
          }

          nextLogLevel = "";
          nextLogLine = "";
          nextLogLineLogged = true;
          String notLogLine;
          Boolean stop = false;

          while (!stop && (notLogLine = reader.readLine()) != null) {
            nextLogLevel = notLogLine.replaceAll(pattern, "$1");
            if (validLogLevel(nextLogLevel)) {
              String nextClassPath = notLogLine.replaceAll(pattern, "$2");
              String nextMessage = notLogLine.replaceAll(pattern, "$3");
              nextLogLine = "Class: " + nextClassPath + "\tMessage: " + nextMessage;
              nextLogLineLogged = false;
              stop = true;
            } else {
              if (!notLogLine.isEmpty())
                messageToLog.append("\n").append(notLogLine);
            }
          }

          logFromLogLevel(logLevel, messageToLog.toString());
        }
      }

      //If the last log line is not logged because was ridden in the second while, we must logged to
      if (!nextLogLineLogged && !nextLogLine.isEmpty() && !nextLogLevel.isEmpty()) {
        logFromLogLevel(nextLogLevel, nextLogLine);
      }

    } catch (Exception e) {
      sink.error("Error reading child process output.", e);
    }
  }

  private Boolean validLogLevel(String logLevel) {
    try {
      Level.valueOf(logLevel);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  private void logFromLogLevel(String logLevel, String logLine) {
    if (validLogLevel(logLevel)) {
      switch (Level.valueOf(logLevel)) {
        case INFO:
          sink.info(logLine);
          break;
        case DEBUG:
          sink.debug(logLine);
          break;
        case WARN:
          sink.warn(logLine);
          break;
        case ERROR:
          sink.error(logLine);
          break;
        case TRACE:
          sink.trace(logLine);
          break;
      }
    }
  }

  /**
   * This method just stops the output of the process from showing up in the local logs.
   * The child's output will still be read (and, thus, the redirect thread will still be
   * alive) to avoid the child process hanging because of lack of output buffer.
   */
  void stop() {
    active = false;
  }

}
