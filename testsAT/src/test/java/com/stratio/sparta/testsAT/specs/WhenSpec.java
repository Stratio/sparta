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
package com.stratio.sparta.testsAT.specs;

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
