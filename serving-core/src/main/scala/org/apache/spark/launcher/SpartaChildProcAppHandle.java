/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package org.apache.spark.launcher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handle implementation for monitoring apps started as a child process.
 */
class SpartaChildProcAppHandle implements SparkAppHandle {

  private static final Logger LOG = Logger.getLogger(SpartaChildProcAppHandle.class.getName());

  private final String secret;
  private final SpartaLauncherServer server;

  private Process childProc;
  private boolean disposed;
  private LauncherConnection connection;
  private List<Listener> listeners;
  private State state;
  private String appId;
  private SpartaOutputRedirector redirector;

  SpartaChildProcAppHandle(String secret, SpartaLauncherServer server) {
    this.secret = secret;
    this.server = server;
    this.state = State.UNKNOWN;
  }

  @Override
  public synchronized void addListener(Listener l) {
    if (listeners == null) {
      listeners = new ArrayList<>();
    }
    listeners.add(l);
  }

  @Override
  public State getState() {
    return state;
  }

  @Override
  public String getAppId() {
    return appId;
  }

  @Override
  public void stop() {
    CommandBuilderUtils.checkState(connection != null, "Application is still not connected.");
    try {
      connection.send(new LauncherProtocol.Stop());
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }

  @Override
  public synchronized void disconnect() {
    if (!disposed) {
      disposed = true;
      if (connection != null) {
        try {
          connection.close();
        } catch (IOException ioe) {
          // no-op.
        }
      }
      server.unregister(this);
      if (redirector != null) {
        redirector.stop();
      }
    }
  }

  @Override
  public synchronized void kill() {
    if (!disposed) {
      disconnect();
    }
    if (childProc != null) {

      try {
        childProc.exitValue();
      } catch (IllegalThreadStateException e) {
        try {
          childProc.destroy();
        } catch (Exception exception) {
          childProc.destroyForcibly();
        }
      }

    }
  }

  String getSecret() {
    return secret;
  }

  void setChildProc(Process childProc, String loggerName) {
    this.childProc = childProc;
    this.redirector = new SpartaOutputRedirector(childProc.getInputStream(), loggerName,
      SpartaLauncher.REDIRECTOR_FACTORY);
  }

  void setConnection(LauncherConnection connection) {
    this.connection = connection;
  }

  SpartaLauncherServer getServer() {
    return server;
  }

  LauncherConnection getConnection() {
    return connection;
  }

  void setState(State s) {
    if (!state.isFinal()) {
      state = s;
      fireEvent(false);
    } else {
      LOG.log(Level.WARNING, "Backend requested transition from final state {0} to {1}.",
        new Object[] { state, s });
    }
  }

  void setAppId(String appId) {
    this.appId = appId;
    fireEvent(true);
  }

  private synchronized void fireEvent(boolean isInfoChanged) {
    if (listeners != null) {
      for (Listener l : listeners) {
        if (isInfoChanged) {
          l.infoChanged(this);
        } else {
          l.stateChanged(this);
        }
      }
    }
  }

}
