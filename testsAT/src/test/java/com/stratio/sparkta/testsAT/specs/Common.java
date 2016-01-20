package com.stratio.sparkta.testsAT.specs;

import com.stratio.specs.CommonG;
import java.net.Socket;
import java.nio.channels.ServerSocketChannel;

public class Common extends CommonG {

    public static String previousFragmentID = "";
    public static String previousFragmentID_2 = "";
    public static String previousPolicyID = "";
    public static String previousPolicyID_2 = "";

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
