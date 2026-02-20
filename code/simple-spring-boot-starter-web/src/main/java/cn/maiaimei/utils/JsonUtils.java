package cn.maiaimei.utils;

import cn.maiaimei.config.JacksonAutoConfiguration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.util.CollectionUtils;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * Utility class for JSON serialization and deserialization.
 */
public final class JsonUtils {

  /**
   * Singleton JsonMapper instance for JSON operations. Initialized using JacksonAutoConfiguration to ensure consistent configuration across the application.
   */
  private static final JsonMapper jsonMapper = new JacksonAutoConfiguration().jsonMapper();

  /**
   * Private constructor to prevent instantiation.
   */
  private JsonUtils() {
  }

  /**
   * Converts an object to JSON string.
   *
   * @param object the object to convert
   * @return JSON string, or null if object is null
   */
  public static String toJson(Object object) {
    if (Objects.isNull(object)) {
      return null;
    }
    return jsonMapper.writeValueAsString(object);
  }

  /**
   * Converts an object to pretty-printed JSON string.
   *
   * @param object the object to convert
   * @return formatted JSON string with indentation, or null if object is null
   */
  public static String toPrettyJson(Object object) {
    if (Objects.isNull(object)) {
      return null;
    }
    return jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
  }

  /**
   * Converts JSON string to an object of the specified class.
   *
   * @param json the JSON string
   * @param clazz the target class
   * @param <T> the type of the target object
   * @return the deserialized object, or null if json is empty
   */
  public static <T> T toObject(String json, Class<T> clazz) {
    if (StringUtilsPlus.isEmpty(json)) {
      return null;
    }
    return jsonMapper.readValue(json, clazz);
  }

  /**
   * Converts JSON string to an object using TypeReference for complex types.
   *
   * @param json the JSON string
   * @param typeReference the type reference for complex types (e.g., Map&lt;String, List&lt;User&gt;&gt;)
   * @param <T> the type of the target object
   * @return the deserialized object, or null if json is empty
   */
  public static <T> T toObject(String json, TypeReference<T> typeReference) {
    if (StringUtilsPlus.isEmpty(json)) {
      return null;
    }
    return jsonMapper.readValue(json, typeReference);
  }

  /**
   * Converts JSON string to a list of objects.
   *
   * @param json the JSON string
   * @param clazz the element class
   * @param <T> the type of list elements
   * @return the list of objects, or null if json is empty
   */
  public static <T> List<T> toList(String json, Class<T> clazz) {
    if (StringUtilsPlus.isEmpty(json)) {
      return null;
    }
    return jsonMapper.readValue(json, jsonMapper.getTypeFactory().constructCollectionType(List.class, clazz));
  }

  /**
   * Converts JSON string to a Map.
   *
   * @param json the JSON string
   * @return the map with string keys and object values, or null if json is empty
   */
  public static Map<String, Object> toMap(String json) {
    if (StringUtilsPlus.isEmpty(json)) {
      return null;
    }
    return jsonMapper.readValue(json, new TypeReference<>() {
    });
  }

  /**
   * Converts a Map to an object of the specified class.
   *
   * @param map the source map
   * @param clazz the target class
   * @param <T> the type of the target object
   * @return the converted object, or null if map is empty
   */
  public static <T> T mapToObject(Map<String, Object> map, Class<T> clazz) {
    if (CollectionUtils.isEmpty(map)) {
      return null;
    }
    return jsonMapper.convertValue(map, clazz);
  }

  /**
   * Creates a deep copy of an object via JSON serialization/deserialization.
   *
   * @param object the object to copy
   * @param clazz the class of the object
   * @param <T> the type of the object
   * @return a deep copy of the object, or null if object is null
   */
  public static <T> T deepCopy(T object, Class<T> clazz) {
    if (Objects.isNull(object)) {
      return null;
    }
    return jsonMapper.convertValue(object, clazz);
  }

  /**
   * Validates if a string is valid JSON.
   *
   * @param json the string to validate
   * @return true if the string is valid JSON, false otherwise
   */
  public static boolean isValidJson(String json) {
    if (StringUtilsPlus.isEmpty(json)) {
      return false;
    }
    try {
      jsonMapper.readTree(json);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

}
