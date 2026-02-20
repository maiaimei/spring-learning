package cn.maiaimei.config;

import cn.maiaimei.filter.RequestLoggingFilter;
import cn.maiaimei.filter.TraceIdFilter;
import cn.maiaimei.filter.constants.FilterConstants;
import cn.maiaimei.filter.properties.TraceIdFilterProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

public class FilterAutoConfiguration {

  @Bean
  @ConditionalOnProperty(name = FilterConstants.TRACE_ID_FILTER_ENABLED, matchIfMissing = true)
  public FilterRegistrationBean<TraceIdFilter> addTraceIdFilterRegistrationBean(TraceIdFilterProperties traceIdFilterProperties) {
    FilterRegistrationBean<TraceIdFilter> filterRegistrationBean = new FilterRegistrationBean<>();
    TraceIdFilter filter = new TraceIdFilter(traceIdFilterProperties);
    filterRegistrationBean.setFilter(filter);
    filterRegistrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
    return filterRegistrationBean;
  }

  //@Bean
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

  @Bean
  public FilterRegistrationBean<RequestLoggingFilter> requestLoggingFilterRegistrationBean() {
    FilterRegistrationBean<RequestLoggingFilter> filterRegistrationBean = new FilterRegistrationBean<>();
    RequestLoggingFilter filter = new RequestLoggingFilter();
    filterRegistrationBean.setFilter(filter);
    filterRegistrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
    return filterRegistrationBean;
  }


}
