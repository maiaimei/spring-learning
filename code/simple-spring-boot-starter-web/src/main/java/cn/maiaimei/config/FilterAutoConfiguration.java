package cn.maiaimei.config;

import cn.maiaimei.filter.AddTraceIdFilter;
import cn.maiaimei.filter.RequestLoggingFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

public class FilterAutoConfiguration {

  @Bean
  public FilterRegistrationBean<AddTraceIdFilter> addTraceIdFilterRegistrationBean() {
    FilterRegistrationBean<AddTraceIdFilter> filterRegistrationBean = new FilterRegistrationBean<>();
    AddTraceIdFilter filter = new AddTraceIdFilter();
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
