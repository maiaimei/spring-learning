package cn.maiaimei.filter;

import cn.maiaimei.filter.model.ContentCachedRequestWrapper;
import cn.maiaimei.filter.properties.RequestLoggingFilterProperties;
import cn.maiaimei.utils.CollectionUtilsPlus;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

@Slf4j
public class RequestLoggingFilter extends OncePerRequestFilter {

  /**
   * Properties for configuring the RequestLoggingFilter=
   */
  private final RequestLoggingFilterProperties requestLoggingFilterProperties;

  /**
   * Configurable filter for excluding certain requests from being processed by
   * this filter
   */
  private final ConfigurableFilter configurableFilter;

  /**
   * Constructs an RequestLoggingFilter with exclude patterns.
   *
   * @param requestLoggingFilterProperties the properties for configuring the
   *                                       filter
   */
  public RequestLoggingFilter(RequestLoggingFilterProperties requestLoggingFilterProperties) {
    this.requestLoggingFilterProperties = requestLoggingFilterProperties;
    this.configurableFilter = CollectionUtilsPlus.isNotEmpty(requestLoggingFilterProperties.getExcludePatterns())
        ? new ConfigurableFilter(requestLoggingFilterProperties.getExcludePatterns())
        : null;
  }

  /**
   * Can be overridden in subclasses for custom filtering control,
   * returning {@code true} to avoid filtering of the given request.
   * <p>
   * The default implementation always returns {@code false}.
   *
   * @param request current HTTP request
   * @return whether the given request should <i>not</i> be filtered
   */
  @Override
  protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
    return configurableFilter != null && configurableFilter.shouldNotFilter(request);
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    ContentCachedRequestWrapper requestToUse = new ContentCachedRequestWrapper(request);
    ContentCachingResponseWrapper responseToUse = new ContentCachingResponseWrapper(response);

    long startTime = System.currentTimeMillis();
    logRequest(requestToUse);

    try {
      filterChain.doFilter(requestToUse, responseToUse);
    } finally {
      long duration = System.currentTimeMillis() - startTime;
      logResponse(responseToUse, duration);
      responseToUse.copyBodyToResponse();
    }
  }

  private void logRequest(ContentCachedRequestWrapper request) {
    Map<String, Object> requestData = new HashMap<>();
    requestData.put("method", request.getMethod());
    requestData.put("uri", request.getRequestURI());

    if (requestLoggingFilterProperties.isIncludeQueryString() && request.getQueryString() != null) {
      requestData.put("queryString", request.getQueryString());
    }

    if (requestLoggingFilterProperties.isIncludeClientInfo()) {
      requestData.put("clientIp", request.getRemoteAddr());
    }

    if (requestLoggingFilterProperties.isIncludeHeaders()) {
      Map<String, String> headers = new HashMap<>();
      request.getHeaderNames().asIterator().forEachRemaining(name ->
          headers.put(name, request.getHeader(name))
      );
      requestData.put("headers", headers);
    }

    if (requestLoggingFilterProperties.isIncludePayload()) {
      String body = request.getBodyAsString();
      if (!body.isEmpty()) {
        requestData.put("payload", body);
      }
    }

    log.info("Request: {}", requestData);
  }

  private void logResponse(ContentCachingResponseWrapper response, long duration) {
    Map<String, Object> responseData = new HashMap<>();
    responseData.put("status", response.getStatus());
    responseData.put("duration", duration);

    if (requestLoggingFilterProperties.isIncludePayload()) {
      byte[] content = response.getContentAsByteArray();
      if (content.length > 0) {
        responseData.put("payload", new String(content, response.getCharacterEncoding()));
      }
    }

    log.info("Response: {}", responseData);
  }
}
