package cn.maiaimei.filter.model;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class RepeatableReadHttpServletRequest extends HttpServletRequestWrapper {

  private final String body;

  public RepeatableReadHttpServletRequest(HttpServletRequest request, String body) {
    super(request);
    this.body = body;
  }

  @Override
  public ServletInputStream getInputStream() throws IOException {
    return new ServletInputStream() {
      private final ByteArrayInputStream bis = new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8));

      @Override
      public int read() throws IOException {
        return bis.read();
      }

      @Override
      public boolean isFinished() {
        return bis.available() == 0;
      }

      @Override
      public boolean isReady() {
        return true;
      }

      @Override
      public void setReadListener(ReadListener readListener) {
      }
    };
  }

  @Override
  public BufferedReader getReader() throws IOException {
    return new BufferedReader(new StringReader(body));
  }
}