package cn.maiaimei.logger;

import java.util.Map;

/**
 * Interface for logging and persisting request and response data.
 */
public interface RequestResponseLogger {

  /**
   * Logs request data.
   *
   * @param requestData the request data to log
   */
  void logRequest(Map<String, Object> requestData);

  /**
   * Logs response data.
   *
   * @param responseData the response data to log
   */
  void logResponse(Map<String, Object> responseData);
}
