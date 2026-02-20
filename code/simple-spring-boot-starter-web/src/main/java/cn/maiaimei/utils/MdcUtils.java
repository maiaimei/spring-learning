package cn.maiaimei.utils;

import static cn.maiaimei.constants.AppConstants.TRACE_ID_MDC_KEY;

import java.util.Map;
import org.slf4j.MDC;

/**
 * Utility class for MDC operations.
 */
public final class MdcUtils {

    /**
     * Private constructor to prevent instantiation.
     */
    private MdcUtils() {
    }

    /**
     * Gets the trace ID from MDC.
     */
    public static String getTraceId() {
        return MDC.get(TRACE_ID_MDC_KEY);
    }

    /**
     * Sets the trace ID to MDC.
     */
    public static void setTraceId(String traceId) {
        MDC.put(TRACE_ID_MDC_KEY, traceId);
    }

    /**
     * Removes the trace ID from MDC.
     */
    public static void removeTraceId() {
        MDC.remove(TRACE_ID_MDC_KEY);
    }

    /**
     * Gets the value associated with the key from MDC.
     */
    public static String get(String key) {
        return MDC.get(key);
    }

    /**
     * Puts a key-value pair into MDC.
     */
    public static void put(String key, String value) {
        MDC.put(key, value);
    }

    /**
     * Removes the value associated with the key from MDC.
     */
    public static void remove(String key) {
        MDC.remove(key);
    }

    /**
     * Clears all entries in MDC.
     */
    public static void clear() {
        MDC.clear();
    }

    /**
     * Returns a copy of the current MDC context map.
     */
    public static Map<String, String> getCopyOfContextMap() {
        return MDC.getCopyOfContextMap();
    }

    /**
     * Sets the MDC context map.
     */
    public static void setContextMap(Map<String, String> contextMap) {
        MDC.setContextMap(contextMap);
    }
}
