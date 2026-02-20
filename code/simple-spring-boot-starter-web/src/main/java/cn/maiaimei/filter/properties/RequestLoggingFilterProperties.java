package cn.maiaimei.filter.properties;

import cn.maiaimei.filter.constants.FilterConstants;
import java.util.List;
import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for RequestLoggingFilter.
 */
@Data
@Component
@ConfigurationProperties(prefix = FilterConstants.REQUEST_LOGGING_FILTER)
@ConditionalOnProperty(name = FilterConstants.REQUEST_LOGGING_FILTER_ENABLED, matchIfMissing = true)
public class RequestLoggingFilterProperties {

  /**
   * Whether to enable the RequestLoggingFilter.
   * <p>
   * Default is true.
   */
  private boolean enabled = true;

  /**
   * Whether to include query string in the log.
   * <p>
   * Default is false.
   */
  private boolean includeQueryString = false;

  /**
   * Whether to include client IP address in the log.
   * <p>
   * Default is false.
   */
  private boolean includeClientInfo = false;

  /**
   * Whether to include request headers in the log.
   * <p>
   * Default is false.
   */
  private boolean includeHeaders = false;

  /**
   * Whether to include request and response payload in the log.
   * <p>
   * Default is false.
   */
  private boolean includePayload = false;

  /**
   * URL patterns to exclude from logging.
   * <p>
   * Supports Ant-style path patterns (e.g., /actuator/**, /health).
   */
  private List<String> excludePatterns;
}
