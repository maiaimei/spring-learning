package cn.maiaimei.constants;

/**
 * Constants for date and time formatting patterns.
 */
public final class DateTimeConstants {

  /**
   * UTC date time format with timezone indicator: yyyy-MM-dd'T'HH:mm:ss.SSS'Z'
   * <p>
   * Example: 2024-01-01T12:30:45.123Z
   */
  public static final String UTC_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

  /**
   * Standard date time format: yyyy-MM-dd HH:mm:ss
   * <p>
   * Example: 2024-01-01 12:30:45
   */
  public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

  /**
   * Date format: yyyy-MM-dd
   * <p>
   * Example: 2024-01-01
   */
  public static final String DATE_FORMAT = "yyyy-MM-dd";

  /**
   * Time format: HH:mm:ss
   * <p>
   * Example: 12:30:45
   */
  public static final String TIME_FORMAT = "HH:mm:ss";

  /**
   * Private constructor to prevent instantiation.
   */
  private DateTimeConstants() {
  }
}
