package cn.maiaimei.filter;

import static cn.maiaimei.constants.AppConstants.TRACE_ID_HEADER;

import cn.maiaimei.filter.properties.TraceIdFilterProperties;
import cn.maiaimei.utils.CollectionUtilsPlus;
import cn.maiaimei.utils.MdcUtils;
import cn.maiaimei.utils.StringUtilsPlus;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.jspecify.annotations.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filter that adds trace ID to request, response and MDC.
 * <p>
 * If the trace ID is present in the request header, it will be used.
 * Otherwise, a new UUID will be generated.
 */
public class TraceIdFilter extends OncePerRequestFilter {

  /**
   * Configurable filter for excluding certain requests from being processed by this filter
   */
  private final ConfigurableFilter configurableFilter;

  /**
   * Constructs an TraceIdFilter with exclude patterns.
   *
   * @param traceIdFilterProperties the properties for configuring the filter
   */
  public TraceIdFilter(TraceIdFilterProperties traceIdFilterProperties) {
    this.configurableFilter = CollectionUtilsPlus.isNotEmpty(traceIdFilterProperties.getExcludePatterns())
        ? new ConfigurableFilter(traceIdFilterProperties.getExcludePatterns())
        : null;
  }

  /**
   * Can be overridden in subclasses for custom filtering control,
   * returning {@code true} to avoid filtering of the given request.
   * <p>The default implementation always returns {@code false}.
   *
   * @param request current HTTP request
   * @return whether the given request should <i>not</i> be filtered
   */
  @Override
  protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
    return configurableFilter != null && configurableFilter.shouldNotFilter(request);
  }

  /**
   * Processes the request by adding trace ID to request attribute, response
   * header and MDC.
   * Clears MDC after request processing.
   */
  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    try {
      String traceId = request.getHeader(TRACE_ID_HEADER);
      if (StringUtilsPlus.isEmpty(traceId)) {
        traceId = UUID.randomUUID().toString();
      }
      request.setAttribute(TRACE_ID_HEADER, traceId);
      response.addHeader(TRACE_ID_HEADER, traceId);
      MdcUtils.setTraceId(traceId);
      filterChain.doFilter(request, response);
    } finally {
      MdcUtils.clear();
    }
  }
}
