package cn.maiaimei.filter;

import static cn.maiaimei.constants.RequestResponseConstants.*;

import cn.maiaimei.filter.model.ContentCachedRequestWrapper;
import cn.maiaimei.filter.properties.RequestLoggingFilterProperties;
import cn.maiaimei.logger.RequestResponseLogger;
import cn.maiaimei.utils.CollectionUtilsPlus;
import cn.maiaimei.utils.ServletUtils;
import cn.maiaimei.utils.StringUtilsPlus;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.util.MultiValueMap;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Filter for logging HTTP request and response data.
 * <p>
 * Logs request method, URI, query parameters, headers, and payload based on configuration.
 * Also logs response status, duration, and payload.
 * Optionally persists data to database via {@link RequestResponseLogger}.
 */
@Slf4j
public class RequestLoggingFilter extends OncePerRequestFilter {

  /**
   * Properties for configuring the RequestLoggingFilter.
   */
  private final RequestLoggingFilterProperties requestLoggingFilterProperties;

  /**
   * Optional logger for persisting request and response data to database.
   */
  private final RequestResponseLogger requestResponseLogger;

  /**
   * Configurable filter for excluding certain requests from being processed.
   */
  private final ConfigurableFilter configurableFilter;

  /**
   * Constructs a RequestLoggingFilter.
   *
   * @param requestLoggingFilterProperties the properties for configuring the filter
   * @param requestResponseLogger optional logger for persisting data to database
   */
  public RequestLoggingFilter(RequestLoggingFilterProperties requestLoggingFilterProperties, RequestResponseLogger requestResponseLogger) {
    this.requestLoggingFilterProperties = requestLoggingFilterProperties;
    this.requestResponseLogger = requestResponseLogger;
    this.configurableFilter = CollectionUtilsPlus.isNotEmpty(requestLoggingFilterProperties.getExcludePatterns())
        ? new ConfigurableFilter(requestLoggingFilterProperties.getExcludePatterns())
        : null;
  }

  /**
   * Determines whether the filter should not be applied to the given request.
   * <p>
   * Returns true if the request matches any of the configured exclude patterns.
   *
   * @param request current HTTP request
   * @return {@code true} if the filter should not be applied
   */
  @Override
  protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
    return configurableFilter != null && configurableFilter.shouldNotFilter(request);
  }

  /**
   * Filters the request and response, logging data before and after processing.
   * <p>
   * Wraps the request and response to enable multiple reads of the body.
   * Logs request data before processing and response data after processing.
   * Measures and logs the request processing duration.
   *
   * @param request the HTTP request
   * @param response the HTTP response
   * @param filterChain the filter chain
   * @throws ServletException if a servlet error occurs
   * @throws IOException if an I/O error occurs
   */
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

  /**
   * Logs request data based on configuration.
   * <p>
   * Collects request method, URI, query parameters, client IP, headers, and payload
   * according to the filter properties. Logs to SLF4J and optionally persists to database.
   *
   * @param request the cached request wrapper
   */
  private void logRequest(ContentCachedRequestWrapper request) {
    Map<String, Object> requestData = new LinkedHashMap<>();
    requestData.put(METHOD, request.getMethod());
    requestData.put(URI, ServletUtils.getRequestPath(request));

    if (requestLoggingFilterProperties.isIncludeQueryString() && Objects.nonNull(request.getQueryString())) {
      MultiValueMap<String, String> queryParams = UriComponentsBuilder.fromUriString("?" + request.getQueryString())
          .build()
          .getQueryParams();
      requestData.put(QUERY_STRING, queryParams);
    }

    if (requestLoggingFilterProperties.isIncludeClientInfo()) {
      requestData.put(CLIENT_IP, request.getRemoteAddr());
    }

    if (requestLoggingFilterProperties.isIncludeHeaders()) {
      Map<String, String> headers = new LinkedHashMap<>();
      request.getHeaderNames().asIterator().forEachRemaining(name -> headers.put(name, request.getHeader(name)));
      requestData.put(HEADERS, headers);
    }

    if (requestLoggingFilterProperties.isIncludePayload()) {
      String body = request.getBodyAsString();
      if (!body.isEmpty()) {
        requestData.put(PAYLOAD, body);
      }
    }

    log.info("Request: {}", requestData);

    if (Objects.nonNull(requestResponseLogger)) {
      requestResponseLogger.logRequest(requestData);
    }
  }

  /**
   * Logs response data based on configuration.
   * <p>
   * Collects response status, processing duration, and payload according to the filter properties.
   * Logs to SLF4J and optionally persists to database.
   *
   * @param response the cached response wrapper
   * @param duration the request processing duration in milliseconds
   */
  private void logResponse(ContentCachingResponseWrapper response, long duration) {
    Map<String, Object> responseData = new LinkedHashMap<>();
    responseData.put(STATUS, response.getStatus());
    responseData.put(DURATION, duration + "ms");

    if (requestLoggingFilterProperties.isIncludePayload()) {
      byte[] content = response.getContentAsByteArray();
      if (content.length > 0) {
        responseData.put(PAYLOAD, StringUtilsPlus.toString(content, response.getCharacterEncoding()));
      }
    }

    log.info("Response: {}", responseData);

    if (Objects.nonNull(requestResponseLogger)) {
      requestResponseLogger.logResponse(responseData);
    }
  }
}
