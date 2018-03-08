/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.testsAT.specs;

import com.stratio.qa.specs.CommonG;
import java.net.Socket;
import java.nio.channels.ServerSocketChannel;

public class Common extends CommonG {
    private ServerSocketChannel serverSocket;
    private Socket socket;

    public ServerSocketChannel getServerSocket() {
        return serverSocket;
    }

    public void setServerSocket(ServerSocketChannel serverSocket) {
        this.serverSocket = serverSocket;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }
}
