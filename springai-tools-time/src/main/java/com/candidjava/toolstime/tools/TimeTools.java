package com.candidjava.toolstime.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.DateTimeException;
import java.time.LocalTime;
import java.time.ZoneId;

/**
 * Lesson: plain @Tool methods the model can call (including error path for invalid zones).
 */
@Component
public class TimeTools {

    private static final Logger log = LoggerFactory.getLogger(TimeTools.class);

    @Tool(name = "getCurrentLocalTime", description = "Get the current time in the server's local timezone")
    String getCurrentLocalTime() {
        String value = LocalTime.now().toString();
        log.info("TOOL getCurrentLocalTime -> {}", value);
        return value;
    }

    @Tool(name = "getCurrentTime", description = "Get the current time in the specified IANA time zone (e.g. Asia/Kolkata, America/New_York)")
    public String getCurrentTime(
            @ToolParam(description = "IANA time zone id, for example Asia/Kolkata") String timeZone) {
        try {
            ZoneId zoneId = ZoneId.of(timeZone);
            String value = LocalTime.now(zoneId).toString();
            log.info("TOOL getCurrentTime(timeZone={}) -> {}", timeZone, value);
            return value;
        }
        catch (DateTimeException ex) {
            log.error("TOOL getCurrentTime FAILED timeZone='{}' — {}", timeZone, ex.getMessage());
            throw new IllegalArgumentException(
                    "Invalid IANA time zone '" + timeZone + "'. Use values like Asia/Kolkata or America/New_York.",
                    ex);
        }
    }
}
