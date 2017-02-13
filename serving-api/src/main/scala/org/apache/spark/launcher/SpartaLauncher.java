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

package org.apache.spark.launcher;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

public class SpartaLauncher extends SparkLauncher {

    private String submit = "";

    public String getSubmit() {
        return submit;
    }

    @Override
    public Process launch() throws IOException {
        ArrayList cmd = new ArrayList();
        String script = CommandBuilderUtils.isWindows() ? "spark-submit.cmd" : "spark-submit";
        cmd.add(CommandBuilderUtils.join(File.separator, this.builder.getSparkHome(), "bin", script));
        cmd.addAll(this.builder.buildSparkSubmitArgs());
        Iterator i$;
        if (CommandBuilderUtils.isWindows()) {
            ArrayList pb = new ArrayList();
            i$ = cmd.iterator();

            while (i$.hasNext()) {
                String e = (String) i$.next();
                pb.add(CommandBuilderUtils.quoteForBatchScript(e));
            }

            cmd = pb;
        }

        ProcessBuilder pb1 = new ProcessBuilder((String[]) cmd.toArray(new String[cmd.size()]));
        i$ = this.builder.childEnv.entrySet().iterator();

        while (i$.hasNext()) {
            Map.Entry e1 = (Map.Entry) i$.next();
            pb1.environment().put(e1.getKey().toString(), e1.getValue().toString());
        }

        StringBuffer command = new StringBuffer();
        for (int i = 0; i < pb1.command().size(); i++) {
            command.append(pb1.command().get(i)).append(" ");
        }
        submit = command.toString();

        return pb1.start();
    }

}