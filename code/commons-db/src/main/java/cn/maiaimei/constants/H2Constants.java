package cn.maiaimei.constants;

public final class H2Constants {

  // 驱动和连接
  public static final String H2_DRIVER_CLASS_NAME = "org.h2.Driver";
  public static final String H2_URL_PREFIX = "jdbc:h2:";
  public static final String H2_MEMORY_URL_PREFIX = "jdbc:h2:mem:";
  public static final String H2_FILE_URL_PREFIX = "jdbc:h2:file:";
  public static final String H2_MEMORY_URL = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE";
  public static final String H2_TCP_URL = "jdbc:h2:tcp://localhost:9092/mem:testdb";
  public static final String H2_WEB_URL = "http://localhost:8082";

  // 默认配置
  public static final String H2_DEFAULT_USERNAME = "sa";
  public static final String H2_DEFAULT_PASSWORD = "";
  public static final String H2_DEFAULT_SCHEMA = "PUBLIC";

  // 连接参数
  public static final String H2_AUTO_SERVER_MODE = "AUTO_SERVER=TRUE";
  public static final String H2_DB_CLOSE_DELAY = "DB_CLOSE_DELAY=-1";

  public static final String H2_USERNAME = getProperty("H2_USERNAME", H2_DEFAULT_USERNAME);
  public static final String H2_PASSWORD = getProperty("H2_PASSWORD", H2_DEFAULT_PASSWORD);

  public static final String[] H2_WEB_SERVER_ARGS = new String[]{"-web", "-webAllowOthers", "-webPort", "8082"};
  public static final String[] H2_TCP_SERVER_ARGS = new String[]{"-tcp", "-tcpAllowOthers", "-tcpPort", "9092"};

  private static String getProperty(String key, String defaultValue) {
    return System.getProperty(key, System.getenv().getOrDefault(key, defaultValue));
  }

  private H2Constants() {
    // 防止实例化
  }
}
