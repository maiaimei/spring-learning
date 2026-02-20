package cn.maiaimei.filter.properties;

import cn.maiaimei.filter.constants.FilterConstants;
import java.util.List;
import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Properties for RequestLoggingFilter.
 */
@Data
@Component
@ConfigurationProperties(prefix = FilterConstants.REQUEST_LOGGING_FILTER)
@ConditionalOnProperty(name = FilterConstants.REQUEST_LOGGING_FILTER_ENABLED, matchIfMissing = true)
public class RequestLoggingFilterProperties {

  /**
   * Whether to enable the RequestLoggingFilter. Default is true.
   */
  private boolean enabled = true;

  private boolean includeQueryString = false;

  private boolean includeClientInfo = false;

  private boolean includeHeaders = false;

  private boolean includePayload = false;

  /**
   * The patterns to exclude from the RequestLoggingFilter.
   */
  private List<String> excludePatterns;
}
