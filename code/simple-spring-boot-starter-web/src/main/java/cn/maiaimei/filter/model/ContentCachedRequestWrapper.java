package cn.maiaimei.filter.model;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.io.*;
import java.nio.charset.StandardCharsets;
import lombok.Getter;

@Getter
public class ContentCachedRequestWrapper extends HttpServletRequestWrapper {

  private final byte[] cachedBody;

  public ContentCachedRequestWrapper(HttpServletRequest request) throws IOException {
    super(request);
    this.cachedBody = request.getInputStream().readAllBytes();
  }

  @Override
  public ServletInputStream getInputStream() {
    return new CachedBodyServletInputStream(cachedBody);
  }

  @Override
  public BufferedReader getReader() {
    return new BufferedReader(new InputStreamReader(new ByteArrayInputStream(cachedBody), StandardCharsets.UTF_8));
  }

  public String getBodyAsString() {
    return new String(cachedBody, StandardCharsets.UTF_8);
  }

  private static class CachedBodyServletInputStream extends ServletInputStream {

    private final ByteArrayInputStream inputStream;

    public CachedBodyServletInputStream(byte[] cachedBody) {
      this.inputStream = new ByteArrayInputStream(cachedBody);
    }

    @Override
    public boolean isFinished() {
      return inputStream.available() == 0;
    }

    @Override
    public boolean isReady() {
      return true;
    }

    @Override
    public void setReadListener(ReadListener listener) {
      throw new UnsupportedOperationException();
    }

    @Override
    public int read() {
      return inputStream.read();
    }
  }
}