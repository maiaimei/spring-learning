package cn.maiaimei.utils;

import java.util.Collection;
import org.jspecify.annotations.Nullable;
import org.springframework.util.CollectionUtils;

/**
 * Utility class for {@link Collection} operations.
 * Complements Spring's {@link CollectionUtils} with additional methods.
 */
public final class CollectionUtilsPlus {

  /**
   * Private constructor to prevent instantiation.
   */
  private CollectionUtilsPlus() {
  }

  /**
   * Checks if the collection is not empty.
   *
   * @param collection the collection to check
   * @return {@code true} if the collection is not null and not empty
   */
  public static boolean isNotEmpty(@Nullable Collection<?> collection) {
    return collection != null && !collection.isEmpty();
  }

  /**
   * Returns the size of the collection, or 0 if null.
   *
   * @param collection the collection to check
   * @return the size of the collection, or 0 if null
   */
  public static int size(@Nullable Collection<?> collection) {
    return collection == null ? 0 : collection.size();
  }

  /**
   * Returns the first element of the collection, or null if empty.
   *
   * @param collection the collection to get the first element from
   * @param <T>        the type of elements in the collection
   * @return the first element, or null if the collection is null or empty
   */
  @Nullable
  public static <T> T getFirst(@Nullable Collection<T> collection) {
    if (collection == null || collection.isEmpty()) {
      return null;
    }
    return collection.iterator().next();
  }
}
