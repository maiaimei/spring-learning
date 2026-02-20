package cn.maiaimei.utils;

import org.jspecify.annotations.Nullable;
import org.springframework.util.StringUtils;

/**
 * Utility class for {@link CharSequence} operations.
 * Complements Spring's {@link StringUtils} with additional methods.
 */
public final class StringUtilsPlus {

  /**
   * Private constructor to prevent instantiation.
   */
  private StringUtilsPlus() {
  }

  /**
   * Check whether the given {@code CharSequence} is empty.
   *
   * @param str the {@code CharSequence} to check (may be {@code null})
   * @return {@code true} if the {@code CharSequence} is {@code null} or
   * <em>empty</em>
   */
  public static boolean isEmpty(@Nullable CharSequence str) {
    return !StringUtils.hasLength(str);
  }

}
