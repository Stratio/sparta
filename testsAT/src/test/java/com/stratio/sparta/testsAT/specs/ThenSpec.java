/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.testsAT.specs;

import com.stratio.qa.specs.CommandExecutionSpec;
import com.stratio.qa.specs.CommonG;
import com.stratio.qa.specs.DcosSpec;
import com.stratio.qa.specs.RestSpec;
import cucumber.api.java.en.Then;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Date;

public class ThenSpec extends BaseSpec {
    protected CommandExecutionSpec commandExecutionSpec;
    protected CommonG commonspec;
    protected RestSpec restSpec;
    protected DcosSpec dcosSpec;

    public ThenSpec(CommonG spec, CommandExecutionSpec cespec, RestSpec rspec, DcosSpec dspec) {
        this.commonspec = spec;
        this.commandExecutionSpec = cespec;
        this.restSpec = rspec;
        this.dcosSpec = dspec;
    }


    @Then(value = "^I execute the command '(.*?)' in all the nodes of my cluster with user '(.+?)' and pem '(.+?)'$")
    public void runInAllNodes(String command, String user, String pem) throws Exception {

        String baseFolder = "/tmp";
        String tmpFileBase = baseFolder + "/parallel-dcos-cli-script-steps-";
        String finalFile = tmpFileBase + new Date().getTime() + ".sh";
        String[] nodes = obtainAllNodes();
        StringBuilder finalCmd = new StringBuilder("#!/bin/bash\n");

        commonspec.getLogger().debug("Creating script file:" + finalFile);

        //Prepare script command in parallel
        for (String node : nodes) {
            finalCmd.append(constructSshParallelCmd(command, user, pem, node));
        }

        finalCmd.append("wait");

        BufferedWriter writer = new BufferedWriter(new FileWriter(finalFile));
        writer.write(finalCmd.toString());
        writer.close();

        commonspec.getLogger().debug("Uploading script file:" + finalFile);
        commandExecutionSpec.copyToRemoteFile(finalFile, baseFolder);

        //Now launch it
        commonspec.getLogger().debug("Giving permissions:" + finalFile);
        commonspec.getRemoteSSHConnection().runCommand("chmod +x " + finalFile);

        commonspec.getLogger().debug("Executing script file:" + finalFile);
        commonspec.runCommandAndGetResult(finalFile);
    }

    public String[] obtainAllNodes() throws Exception {
        commonspec.getLogger().debug("Retrieving nodes from dcos-cli");

        commonspec.runCommandAndGetResult("dcos node | tail -n +2 | cut -d \" \" -f 1 | tr '\\n' ';'");
        String cmdResult = commonspec.getCommandResult();
        commonspec.getLogger().debug("Command result:" + cmdResult);

        //Remove last ; and split
        String[] splitted = cmdResult.substring(0, cmdResult.length() - 1).split(";");
        commonspec.getLogger().debug("Nodes found: " + splitted.length);

        return splitted;
    }
    private String constructSshParallelCmd(String command, String user, String pem, String node) {
        return
                "ssh -o StrictHostKeyChecking=no -o " +
                        "UserKnownHostsFile=/dev/null " +
                        "-i /" + pem + " " +
                        user + "@" + node + " " +
                        command + " &\n";

    }

}

