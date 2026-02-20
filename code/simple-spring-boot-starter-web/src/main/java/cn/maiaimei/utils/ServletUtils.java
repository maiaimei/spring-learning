package cn.maiaimei.utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.Nullable;

/**
 * Utility class for Servlet request and response operations.
 */
public final class ServletUtils {

  /**
   * Private constructor to prevent instantiation.
   */
  private ServletUtils() {
  }

  /**
   * Gets the request URI without the context path.
   *
   * @param request the HTTP request
   * @return the request URI without context path
   */
  public static String getRequestPath(HttpServletRequest request) {
    String contextPath = request.getContextPath();
    String requestUri = request.getRequestURI();
    if (StringUtilsPlus.isEmpty(contextPath)) {
      return requestUri;
    }
    return requestUri.substring(contextPath.length());
  }

  /**
   * Gets the client IP address from the request.
   *
   * @param request the HTTP request
   * @return the client IP address
   */
  public static String getClientIp(HttpServletRequest request) {
    String ip = request.getHeader("X-Forwarded-For");
    if (isValidIp(ip)) {
      return ip.split(",")[0].trim();
    }
    ip = request.getHeader("X-Real-IP");
    if (isValidIp(ip)) {
      return ip;
    }
    return request.getRemoteAddr();
  }

  /**
   * Checks if the request is an AJAX request.
   *
   * @param request the HTTP request
   * @return {@code true} if the request is an AJAX request
   */
  public static boolean isAjaxRequest(HttpServletRequest request) {
    String header = request.getHeader("X-Requested-With");
    return "XMLHttpRequest".equalsIgnoreCase(header);
  }

  /**
   * Sets no-cache headers on the response.
   *
   * @param response the HTTP response
   */
  public static void setNoCacheHeaders(HttpServletResponse response) {
    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
    response.setHeader("Pragma", "no-cache");
    response.setHeader("Expires", "0");
  }

  /**
   * Validates the given IP address string.
   *
   * @param ip the IP address string to validate
   * @return {@code true} if the IP address is valid; {@code false} otherwise
   */
  private static boolean isValidIp(@Nullable String ip) {
    return ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip);
  }
}
