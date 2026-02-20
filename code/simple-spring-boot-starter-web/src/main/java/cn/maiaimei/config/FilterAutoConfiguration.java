package cn.maiaimei.config;

import cn.maiaimei.filter.RequestLoggingFilter;
import cn.maiaimei.filter.TraceIdFilter;
import cn.maiaimei.filter.constants.FilterConstants;
import cn.maiaimei.filter.properties.RequestLoggingFilterProperties;
import cn.maiaimei.filter.properties.TraceIdFilterProperties;
import cn.maiaimei.logger.RequestResponseLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

/**
 * Auto-configuration for filters.
 */
public class FilterAutoConfiguration {

  /**
   * Registers the TraceIdFilter.
   * <p>
   * This filter adds a trace ID to the request, response, and MDC for request tracing.
   * It runs with the highest precedence to ensure trace ID is available for all subsequent filters.
   *
   * @param traceIdFilterProperties the properties for configuring the filter
   * @return the filter registration bean
   */
  @Bean
  @ConditionalOnProperty(name = FilterConstants.TRACE_ID_FILTER_ENABLED, matchIfMissing = true)
  public FilterRegistrationBean<TraceIdFilter> traceIdFilterRegistrationBean(
      TraceIdFilterProperties traceIdFilterProperties) {
    FilterRegistrationBean<TraceIdFilter> filterRegistrationBean = new FilterRegistrationBean<>();
    TraceIdFilter filter = new TraceIdFilter(traceIdFilterProperties);
    filterRegistrationBean.setFilter(filter);
    filterRegistrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
    return filterRegistrationBean;
  }

  /**
   * Registers the CommonsRequestLoggingFilter (disabled by default).
   * <p>
   * This is a Spring-provided filter for basic request logging.
   * Uncomment the @Bean annotation to enable it.
   *
   * @return the filter registration bean
   */
  // @Bean
  public FilterRegistrationBean<CommonsRequestLoggingFilter> commonsRequestLoggingFilterRegistrationBean() {
    FilterRegistrationBean<CommonsRequestLoggingFilter> filterRegistrationBean = new FilterRegistrationBean<>();
    CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
    filter.setIncludeClientInfo(true);
    filter.setIncludeHeaders(true);
    filter.setIncludeQueryString(true);
    filter.setIncludePayload(true);
    filterRegistrationBean.setFilter(filter);
    filterRegistrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
    return filterRegistrationBean;
  }

  /**
   * Registers the RequestLoggingFilter.
   * <p>
   * This filter logs request and response data based on configuration.
   * It can optionally persist data to a database via RequestResponseLogger.
   *
   * @param requestLoggingFilterProperties the properties for configuring the filter
   * @param requestResponseLogger          optional logger for persisting data to database
   * @return the filter registration bean
   */
  @Bean
  @ConditionalOnProperty(name = FilterConstants.REQUEST_LOGGING_FILTER_ENABLED, matchIfMissing = true)
  public FilterRegistrationBean<RequestLoggingFilter> requestLoggingFilterRegistrationBean(
      RequestLoggingFilterProperties requestLoggingFilterProperties,
      @Autowired(required = false) RequestResponseLogger requestResponseLogger) {
    FilterRegistrationBean<RequestLoggingFilter> filterRegistrationBean = new FilterRegistrationBean<>();
    RequestLoggingFilter filter = new RequestLoggingFilter(requestLoggingFilterProperties, requestResponseLogger);
    filterRegistrationBean.setFilter(filter);
    filterRegistrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
    return filterRegistrationBean;
  }

}
