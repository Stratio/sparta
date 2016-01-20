package com.stratio.sparkta.testsAT.specs;

import cucumber.api.java.en.When;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;

import static com.stratio.assertions.Assertions.assertThat;

public class WhenSpec extends BaseSpec {

    public WhenSpec(Common spec) {
	    this.commonspec = spec;
    }

    @When("^I start a socket in '([^:]+?):(.+?)?'$")
    public void startSocket(String socketHost, String socketPort) throws java.io.IOException, java.net.UnknownHostException {
        assertThat(socketHost).isNotEmpty();
        assertThat(socketPort).isNotEmpty();
        commonspec.getLogger().info("Creating socket at: " + socketHost + ":" + socketPort);
        commonspec.setServerSocket(ServerSocketChannel.open());
        commonspec.getServerSocket().socket().bind(new InetSocketAddress(socketHost, Integer.parseInt(socketPort)));
    }


    @When("^I send data from file '([^:]+?)' to socket$")
    public void sendDataToSocket(String baseData) throws java.io.IOException, java.io.FileNotFoundException {
        String line = "";
        PrintStream out = new PrintStream(commonspec.getServerSocket().socket().accept().getOutputStream());
        BufferedReader br = new BufferedReader(new FileReader(baseData));

        while ((line = br.readLine()) != null) {
	        // use comma as separator
			String[] data = line.split(",");
            out.println(line);
		}
        out.flush();
    }

}
