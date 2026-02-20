package cn.maiaimei.filter;

import cn.maiaimei.utils.CollectionUtilsPlus;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.Setter;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

/**
 * Utility class for filter URL pattern matching.
 */
public class ConfigurableFilter {

  /**
   * PathMatcher for matching URL patterns.
   */
  private final PathMatcher pathMatcher = new AntPathMatcher();

  /**
   * The URL patterns to include.
   * -- SETTER --
   * Sets the URL patterns to include.
   */
  @Setter
  private List<String> includePatterns;

  /**
   * The URL patterns to exclude.
   * -- SETTER --
   * Sets the URL patterns to exclude.
   */
  @Setter
  private List<String> excludePatterns;

  /**
   * Constructs a ConfigurableFilter with exclude patterns.
   *
   * @param excludePatterns the URL patterns to exclude
   */
  public ConfigurableFilter(List<String> excludePatterns) {
    this.excludePatterns = excludePatterns;
  }

  /**
   * Constructs a ConfigurableFilter with include and exclude patterns.
   *
   * @param includePatterns the URL patterns to include
   * @param excludePatterns the URL patterns to exclude
   */
  public ConfigurableFilter(List<String> includePatterns, List<String> excludePatterns) {
    this.includePatterns = includePatterns;
    this.excludePatterns = excludePatterns;
  }

  /**
   * Determines whether the filter should not be applied to the given request.
   *
   * @param request the HTTP request
   * @return {@code true} if the filter should not be applied
   */
  public boolean shouldNotFilter(HttpServletRequest request) {
    String requestPath = request.getRequestURI();

    if (CollectionUtilsPlus.isNotEmpty(excludePatterns)) {
      for (String pattern : excludePatterns) {
        if (pathMatcher.match(pattern, requestPath)) {
          return true;
        }
      }
    }

    if (CollectionUtilsPlus.isNotEmpty(includePatterns)) {
      for (String pattern : includePatterns) {
        if (pathMatcher.match(pattern, requestPath)) {
          return false;
        }
      }
      return true;
    }

    return false;
  }
}
