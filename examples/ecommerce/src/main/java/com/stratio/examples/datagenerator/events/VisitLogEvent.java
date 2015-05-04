/**
 * Copyright (C) 2015 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stratio.examples.datagenerator.events;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class VisitLogEvent implements Event {

    private final long timestamp;
    private final String ipHost;
    private final String userAgent;
    private final String userLog;
    private final String responseCode;
    private final String operatingSystem;

    public VisitLogEvent(long timestamp, String ipHost, String userAgent, String userLog, String responseCode, String operatingSystem) {
        this.timestamp = timestamp;
        this.ipHost = ipHost;
        this.userAgent = userAgent;
        this.userLog = userLog;
        this.responseCode = responseCode;
        this.operatingSystem = operatingSystem;
    }

    public static VisitLogEvent getInstance(Object[] args) {
        long timestamp = Long.parseLong(args[0].toString());
        String ipHost = args[1].toString();
        String userAgent = args[2].toString();
        String user = args[3].toString();
        String responseCode = args[4].toString();
        String operatingSystem = args[5].toString();
        return new VisitLogEvent(timestamp, ipHost, userAgent, user, responseCode, operatingSystem);
    }

    public String toJsonOutput() {
        Gson gson = new Gson();
        JsonObject eventObject = new JsonObject();
        eventObject.addProperty("timestamp", timestamp);
        eventObject.addProperty("ipHost", ipHost);
        eventObject.addProperty("userAgent", userAgent);
        eventObject.addProperty("userLog", userLog);
        eventObject.addProperty("responseCode", responseCode);
        eventObject.addProperty("operatingSystem", operatingSystem);

        return gson.toJson(eventObject);
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getIpHost() {
        return ipHost;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public String getUserLog() {
        return userLog;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public String getOperatingSystem() {
        return operatingSystem;
    }
}
