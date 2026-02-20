package cn.maiaimei.constants;

/**
 * Application constants.
 */
public final class AppConstants {

  /**
   * HTTP header name for trace ID.
   */
  public static final String TRACE_ID_HEADER = "X-Trace-Id";

  /**
   * MDC key for trace ID.
   */
  public static final String TRACE_ID_MDC_KEY = "traceId";

  /**
   * Private constructor to prevent instantiation.
   */
  private AppConstants() {
  }
}
