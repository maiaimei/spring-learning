package cn.maiaimei.utils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Utility class for generating unique IDs based on timestamp and sequence.
 * <p>
 * ID format: yyyyMMddHHmmssSSS + 5-digit sequence (e.g., 20240101120000000 +
 * 00001)
 */
public final class IdGenerator {

  /**
   * Maximum value for the sequence counter.
   */
  private static final int SEQUENCE_MAX = 99999;

  /**
   * Format string for the sequence number (5 digits with leading zeros).
   */
  private static final String SEQUENCE_FORMAT = "%05d";

  /**
   * Date time format pattern for the timestamp part of the ID.
   */
  private static final String DATE_TIME_FORMAT = "yyyyMMddHHmmssSSS";

  /**
   * Date time formatter for formatting timestamps.
   */
  private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);

  /**
   * Atomic counter for generating sequential numbers.
   */
  private static final AtomicInteger sequence = new AtomicInteger(0);

  /**
   * Private constructor to prevent instantiation.
   */
  private IdGenerator() {
    throw new UnsupportedOperationException("Utility class cannot be instantiated");
  }

  /**
   * Generates the next unique ID.
   * <p>
   * The ID consists of a timestamp (17 digits) and a sequence number (5 digits),
   * resulting in a 22-digit number. The sequence resets to 0 after reaching
   * 99999.
   *
   * @return a unique ID as BigDecimal
   */
  public static BigDecimal nextId() {
    String timestamp = LocalDateTime.now().format(DATE_TIME_FORMATTER);

    int currentSequence = sequence.updateAndGet(current -> {
      if (current >= SEQUENCE_MAX) {
        return 0;
      }
      return current + 1;
    });

    String sequenceStr = String.format(SEQUENCE_FORMAT, currentSequence);

    return new BigDecimal(timestamp + sequenceStr);
  }
}