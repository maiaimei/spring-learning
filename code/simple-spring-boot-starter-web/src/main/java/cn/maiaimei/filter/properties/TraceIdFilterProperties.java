package cn.maiaimei.filter.properties;

import cn.maiaimei.filter.constants.FilterConstants;
import java.util.List;
import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = FilterConstants.TRACE_ID_FILTER)
@ConditionalOnProperty(name = FilterConstants.TRACE_ID_FILTER_ENABLED, matchIfMissing = true)
public class TraceIdFilterProperties {

  private boolean enabled = true;
  private List<String> excludePatterns;
}
