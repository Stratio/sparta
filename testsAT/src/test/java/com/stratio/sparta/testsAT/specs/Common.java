package com.stratio.sparta.testsAT.specs;

import com.stratio.specs.CommonG;
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
