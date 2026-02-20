package cn.maiaimei.filter;

import static cn.maiaimei.constants.AppConstants.TRACE_ID_HEADER;

import cn.maiaimei.utils.CharSequenceUtils;
import cn.maiaimei.utils.MdcUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filter that adds trace ID to request, response and MDC.
 * <p>
 * If the trace ID is present in the request header, it will be used.
 * Otherwise, a new UUID will be generated.
 */
public class AddTraceIdFilter extends OncePerRequestFilter {

  /**
   * Processes the request by adding trace ID to request attribute, response header and MDC.
   * Clears MDC after request processing.
   */
  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
    try {
      String traceId = request.getHeader(TRACE_ID_HEADER);
      if (CharSequenceUtils.isEmpty(traceId)) {
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
