package cn.maiaimei.constants;

/**
 * Constants for request and response.
 */
public final class RequestResponseConstants {

  /**
   * Request data keys.
   */
  public static final String METHOD = "method";
  public static final String URI = "uri";
  public static final String QUERY_STRING = "queryString";
  public static final String CLIENT_IP = "clientIp";
  public static final String HEADERS = "headers";

  /**
   * Request/Response data keys.
   */
  public static final String PAYLOAD = "payload";

  /**
   * Response data keys.
   */
  public static final String STATUS = "status";
  public static final String DURATION = "duration";

  /**
   * Private constructor to prevent instantiation.
   */
  private RequestResponseConstants() {
  }
}
