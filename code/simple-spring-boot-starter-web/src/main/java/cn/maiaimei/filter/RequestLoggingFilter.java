package cn.maiaimei.filter;

import cn.maiaimei.filter.model.RepeatableReadHttpServletRequest;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
public class RequestLoggingFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
    // 手动读取请求体并记录
    String requestBody = getRequestBody(request);
    if (StringUtils.hasLength(requestBody)) {
      log.info("Request Body: {}", requestBody);
    }

    // 创建可重复读取的请求包装器
    HttpServletRequest requestToUse = new RepeatableReadHttpServletRequest(request, requestBody);

    filterChain.doFilter(requestToUse, response);
  }

  private String getRequestBody(HttpServletRequest request) throws IOException {
    StringBuilder body = new StringBuilder();
    try (BufferedReader reader = request.getReader()) {
      String line;
      while ((line = reader.readLine()) != null) {
        body.append(line);
      }
    }
    return body.toString();
  }
}
