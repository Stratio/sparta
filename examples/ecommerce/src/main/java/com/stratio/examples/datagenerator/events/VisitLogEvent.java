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
