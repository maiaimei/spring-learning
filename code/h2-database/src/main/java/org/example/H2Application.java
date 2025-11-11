package org.example;

import static org.example.constants.H2Constants.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class H2Application {

  private static final Logger log = LoggerFactory.getLogger(H2Application.class);

  public static void main(String[] args) {
    try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
      context.register(H2DatabaseConfig.class, H2DatabaseInitializer.class);
      context.refresh();
      printH2Info();
      setupShutdownHook(context);
      keepRunning();
    } catch (Exception e) {
      log.error("H2数据库服务启动失败", e);
    }
  }

  private static void printH2Info() {
    log.info("===========================================");
    log.info("H2数据库服务已启动");
    log.info("Web控制台: {}", WEB_URL);
    log.info("JDBC URL: {}", TCP_URL);
    log.info("用户名: {}", USERNAME);
    log.info("密码: {}", PASSWORD);
    log.info("===========================================");
  }

  private static void setupShutdownHook(AnnotationConfigApplicationContext context) {
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      log.info("正在关闭H2数据库服务...");
      context.close();
      log.info("H2数据库服务已关闭");
    }));
  }

  private static void keepRunning() {
    try {
      Thread.currentThread().join();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
}